package com.veyru.service.photo;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.veyru.dto.request.photo.CreatePhotoRequest;
import com.veyru.dto.response.comment.CommentResponse;
import com.veyru.dto.response.like.LikeResponse;
import com.veyru.dto.response.photo.PhotoDetailResponse;
import com.veyru.dto.response.photo.PhotoResponse;
import com.veyru.event.PhotoCreatedEvent;
import com.veyru.model.Comment;
import com.veyru.model.Like;
import com.veyru.model.Photo;
import com.veyru.model.Photo.EmbeddedUser;
import com.veyru.model.User;
import com.veyru.repository.CommentRepository;
import com.veyru.repository.FavoriteRepository;
import com.veyru.repository.LikeRepository;
import com.veyru.repository.PhotoRepository;
import com.veyru.repository.ShareRepository;
import com.veyru.service.graph.Neo4jGraphService;
import com.veyru.service.user.UserAvatarCacheService;
import com.veyru.service.user.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoService implements IPhotoService {

    private final CloudinaryService cloudinaryService;
    private final PhotoRepository photoRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;
    private final ShareRepository shareRepository;
    private final PhotoConversionService photoConversionService;
    private final ApplicationEventPublisher eventPublisher;
    private final MongoTemplate mongoTemplate;
    private final UserAvatarCacheService userAvatarCacheService;
    private final Neo4jGraphService neo4jGraphService;

    @Override
    public PhotoResponse createPhoto(CreatePhotoRequest request) {
        log.info("Creating new photo with caption: {}", request.getCaption());
        Map<String, Object> uploadResult = cloudinaryService.uploadImage(request.getImage());
        String imageUrl = (String) uploadResult.get("secure_url");
        log.info("Image uploaded successfully, URL: {}", imageUrl);

        // Create photo entity
        User currentUser = userService.getCurrentUser();

        EmbeddedUser embeddedUser = new EmbeddedUser();
        embeddedUser.setUserId(currentUser.getId());
        embeddedUser.setUsername(currentUser.getUsername());

        Photo photo = new Photo();
        photo.setUser(embeddedUser);
        photo.setImageUrl(imageUrl);
        photo.setCaption(request.getCaption());
        photo.setCreatedAt(Instant.now());

        // Handle tags
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            List<String> tagNames = request.getTags().stream()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .distinct()
                    .toList();
            photo.setTags(tagNames);
        }

        Photo savedPhoto = photoRepository.save(photo);

        // Sync to Neo4j graph - create photo node
        try {
            neo4jGraphService.upsertPhoto(
                    savedPhoto.getId(),
                    currentUser.getId(),
                    currentUser.getUsername(),
                    savedPhoto.getImageUrl(),
                    savedPhoto.getCaption(),
                    savedPhoto.getTags(),
                    0, 0, 0,
                    savedPhoto.getCreatedAt());
            log.debug("Synced photo to Neo4j: {}", savedPhoto.getId());
        } catch (Exception e) {
            log.warn("Failed to sync photo to Neo4j: {}", e.getMessage());
        }

        Query query = new Query(Criteria.where("_id").is(currentUser.getId()));
        Update update = new Update().inc("photoCount", 1);
        mongoTemplate.updateFirst(query, update, User.class);

        // Publish event to update followers' feeds asynchronously
        eventPublisher.publishEvent(new PhotoCreatedEvent(this, savedPhoto.getId(), currentUser.getId()));

        log.info("Photo created successfully with ID: {}", savedPhoto.getId());
        return photoConversionService.convertToPhotoResponse(savedPhoto, currentUser);
    }

    @Override
    public Page<PhotoResponse> getAllPhotos(int page, int size) {
        log.info("Fetching all photos - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Photo> photos = photoRepository.findAllByOrderByCreatedAtDesc(pageable);

        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
        }
        final User finalCurrentUser = currentUser;

        return photos.map(photo -> photoConversionService.convertToPhotoResponse(photo, finalCurrentUser));
    }

    @Override
    public Page<PhotoResponse> getPhotosByUserId(String userId, int page, int size) {
        log.info("Fetching photos for user ID: {} - page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Photo> photos = photoRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);

        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
        }
        final User finalCurrentUser = currentUser;

        return photos.map(photo -> photoConversionService.convertToPhotoResponse(photo, finalCurrentUser));
    }

    @Override
    public PhotoDetailResponse getPhotoById(String photoId) {
        log.info("Fetching photo details for ID: {}", photoId);

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

        List<Like> likes = likeRepository.findByPhotoIdOrderByCreatedAtDesc(photoId);
        List<Comment> comments = commentRepository.findByPhotoIdOrderByCreatedAtAsc(photoId);

        // Convert to response
        PhotoDetailResponse response = convertToPhotoDetailResponse(photo);
        response.setLikes(convertToLikeResponses(likes));
        response.setComments(convertToCommentResponses(comments));

        return response;
    }

    @Override
    @Transactional
    public void deletePhoto(String photoId) {
        log.info("Deleting photo with ID: {}", photoId);

        // Verify photo exists and user has permission to delete
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

        User currentUser = userService.getCurrentUser();

        // Check if current user is the owner of the photo or admin
        if (!photo.getUser().getUserId().equals(currentUser.getId()) &&
                !currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new RuntimeException("You don't have permission to delete this photo");
        }

        try {
            // Delete image from Cloudinary
            String publicId = cloudinaryService.extractPublicIdFromUrl(photo.getImageUrl());
            if (publicId != null) {
                cloudinaryService.deleteImage(publicId);
                log.info("Image deleted from Cloudinary for photo ID: {}", photoId);
            }

            likeRepository.deleteAllByPhotoId(photoId);
            commentRepository.deleteAllByPhotoId(photoId);
            favoriteRepository.deleteAllByPhotoId(photoId);
            shareRepository.deleteAllByPhotoId(photoId);
            log.info("Deleted all likes, comments, favorites and shares for photo ID: {}", photoId);

            photoRepository.deleteById(photoId);

            Query query = new Query(Criteria.where("_id").is(photo.getUser().getUserId()));
            Update update = new Update().inc("photoCount", -1);
            mongoTemplate.updateFirst(query, update, User.class);

            log.info("Photo deleted successfully with ID: {}", photoId);

        } catch (Exception e) {
            log.error("Failed to delete photo with ID: {}", photoId, e);
            throw new RuntimeException("Failed to delete photo", e);
        }
    }

    // Keep these methods for PhotoDetailResponse (specific to this service)
    private PhotoDetailResponse convertToPhotoDetailResponse(Photo photo) {
        PhotoDetailResponse response = modelMapper.map(photo, PhotoDetailResponse.class);
        if (photo.getUser() != null) {
            response.setUsername(photo.getUser().getUsername());
            response.setUserImageUrl(userAvatarCacheService.getAvatar(photo.getUser().getUserId()));
        }
        response.setLikeCount((int) photo.getLikeCount());
        response.setCommentCount((int) photo.getCommentCount());
        response.setShareCount((int) photo.getShareCount());

        try {
            User currentUser = userService.getCurrentUser();
            response.setLikedByCurrentUser(
                    likeRepository.existsByPhotoIdAndUserId(photo.getId(), currentUser.getId()));
            response.setSavedByCurrentUser(
                    favoriteRepository.existsByUserIdAndPhotoId(currentUser.getId(), photo.getId()));
        } catch (Exception e) {
            response.setLikedByCurrentUser(false);
            response.setSavedByCurrentUser(false);
        }

        return response;
    }

    private List<LikeResponse> convertToLikeResponses(List<Like> likes) {
        if (likes.isEmpty())
            return List.of();
        List<String> userIds = likes.stream().map(Like::getUserId).distinct().toList();
        Map<String, User> usersMap = userService.findUsersByIds(userIds);
        return likes.stream().map(like -> {
            LikeResponse res = modelMapper.map(like, LikeResponse.class);
            User user = usersMap.get(like.getUserId());
            if (user != null) {
                res.setUsername(user.getUsername());
                res.setUserImageUrl(userAvatarCacheService.getAvatar(user.getId()));
            }
            return res;
        }).toList();
    }

    private List<CommentResponse> convertToCommentResponses(List<Comment> comments) {
        if (comments.isEmpty())
            return List.of();
        return comments.stream().map(comment -> {
            CommentResponse res = modelMapper.map(comment, CommentResponse.class);
            if (comment.getUser() != null) {
                res.setUsername(comment.getUser().getUsername());
                res.setUserImageUrl(userAvatarCacheService.getAvatar(comment.getUser().getUserId()));
            }
            return res;
        }).toList();
    }
}
