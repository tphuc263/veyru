package com.veyru.domain.service.photo;

import com.veyru.adapter.in.dto.request.photo.CreatePhotoRequest;
import com.veyru.adapter.in.dto.response.comment.CommentResponse;
import com.veyru.adapter.in.dto.response.like.LikeResponse;
import com.veyru.adapter.in.dto.response.photo.PhotoDetailResponse;
import com.veyru.adapter.in.dto.response.photo.PhotoResponse;
import com.veyru.application.event.PhotoCreatedEvent;
import com.veyru.application.port.out.*;
import com.veyru.domain.exception.ApiException;
import com.veyru.domain.exception.ErrorCode;
import com.veyru.domain.model.Comment;
import com.veyru.domain.model.Like;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.Photo.EmbeddedUser;
import com.veyru.domain.model.User;
import com.veyru.domain.service.user.UserAvatarCacheService;
import com.veyru.domain.service.user.UserService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class PhotoService {
  private static final Logger log = LoggerFactory.getLogger(PhotoService.class);
  private final CloudinaryService cloudinaryService;
  private final PhotoRepository photoRepository;
  private final UserService userService;
  private final LikeRepository likeRepository;
  private final FavoriteRepository favoriteRepository;
  private final CommentRepository commentRepository;
  private final ShareRepository shareRepository;
  private final PhotoConversionService photoConversionService;
  private final ApplicationEventPublisher eventPublisher;
  private final MongoTemplate mongoTemplate;
  private final UserAvatarCacheService userAvatarCacheService;

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
      List<String> tagNames =
          request.getTags().stream().map(String::trim).map(String::toLowerCase).distinct().toList();
      photo.setTags(tagNames);
    }
    Photo savedPhoto = photoRepository.save(photo);
    Query query = new Query(Criteria.where("_id").is(currentUser.getId()));
    Update update = new Update().inc("photoCount", 1);
    mongoTemplate.updateFirst(query, update, User.class);
    // Publish event to update followers' feeds asynchronously
    eventPublisher.publishEvent(
        new PhotoCreatedEvent(this, savedPhoto.getId(), currentUser.getId()));
    log.info("Photo created successfully with ID: {}", savedPhoto.getId());
    return photoConversionService.convertToPhotoResponse(savedPhoto, currentUser);
  }

  public Page<PhotoResponse> getAllPhotos(int page, int size) {
    log.info("Fetching all photos - page: {}, size: {}", page, size);
    Pageable pageable = PageRequest.of(page, size);
    Page<Photo> photos = photoRepository.findAllByOrderByCreatedAtDesc(pageable);
    User currentUser = null;

    currentUser = userService.getCurrentUser();

    final User finalCurrentUser = currentUser;
    return photos.map(
        photo -> photoConversionService.convertToPhotoResponse(photo, finalCurrentUser));
  }

  public Page<PhotoResponse> getPhotosByUserId(String userId, int page, int size) {
    log.info("Fetching photos for user ID: {} - page: {}, size: {}", userId, page, size);
    Pageable pageable = PageRequest.of(page, size);
    Page<Photo> photos = photoRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);
    User currentUser = null;

    currentUser = userService.getCurrentUser();

    final User finalCurrentUser = currentUser;
    return photos.map(
        photo -> photoConversionService.convertToPhotoResponse(photo, finalCurrentUser));
  }

  public PhotoDetailResponse getPhotoById(String photoId) {
    log.info("Fetching photo details for ID: {}", photoId);
    Photo photo =
        photoRepository
            .findById(photoId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    List<Like> likes = likeRepository.findByPhotoIdOrderByCreatedAtDesc(photoId);
    List<Comment> comments = commentRepository.findByPhotoIdOrderByCreatedAtAsc(photoId);
    // Convert to response
    PhotoDetailResponse response = convertToPhotoDetailResponse(photo);
    response.setLikes(convertToLikeResponses(likes));
    response.setComments(convertToCommentResponses(comments));
    return response;
  }

  @Transactional
  public void deletePhoto(String photoId) {
    log.info("Deleting photo with ID: {}", photoId);
    // Verify photo exists and user has permission to delete
    Photo photo =
        photoRepository
            .findById(photoId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    User currentUser = userService.getCurrentUser();
    // Check if current user is the owner of the photo or admin
    if (!photo.getUser().getUserId().equals(currentUser.getId())
        && !currentUser.getRole().name().equals("ROLE_ADMIN")) {
      throw new ApiException(ErrorCode.ACCESS_DENIED);
    }

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
  }

  // Keep these methods for PhotoDetailResponse (specific to this service)
  private PhotoDetailResponse convertToPhotoDetailResponse(Photo photo) {
    PhotoDetailResponse response = new PhotoDetailResponse();
    response.setId(photo.getId());
    response.setImageUrl(photo.getImageUrl());
    response.setCaption(photo.getCaption());
    response.setCreatedAt(photo.getCreatedAt());
    response.setTags(photo.getTags());
    if (photo.getUser() != null) {
      response.setUsername(photo.getUser().getUsername());
      response.setUserId(photo.getUser().getUserId());
      response.setUserImageUrl(userAvatarCacheService.getAvatar(photo.getUser().getUserId()));
    }
    response.setLikeCount((int) photo.getLikeCount());
    response.setCommentCount((int) photo.getCommentCount());
    response.setShareCount((int) photo.getShareCount());

    User currentUser = userService.getCurrentUser();
    response.setLikedByCurrentUser(
        likeRepository.existsByPhotoIdAndUserId(photo.getId(), currentUser.getId()));
    response.setSavedByCurrentUser(
        favoriteRepository.existsByUserIdAndPhotoId(currentUser.getId(), photo.getId()));

    return response;
  }

  private List<LikeResponse> convertToLikeResponses(List<Like> likes) {
    if (likes.isEmpty()) return List.of();
    List<String> userIds = likes.stream().map(Like::getUserId).distinct().toList();
    Map<String, User> usersMap = userService.findUsersByIds(userIds);
    return likes.stream()
        .map(
            like -> {
              LikeResponse res = new LikeResponse();
              res.setId(like.getId());
              res.setUserId(like.getUserId());
              res.setCreatedAt(like.getCreatedAt());
              User user = usersMap.get(like.getUserId());
              if (user != null) {
                res.setUsername(user.getUsername());
                res.setUserImageUrl(userAvatarCacheService.getAvatar(user.getId()));
              }
              return res;
            })
        .toList();
  }

  private List<CommentResponse> convertToCommentResponses(List<Comment> comments) {
    if (comments.isEmpty()) return List.of();
    return comments.stream()
        .map(
            comment -> {
              CommentResponse res = new CommentResponse();
              res.setId(comment.getId());
              res.setPhotoId(comment.getPhotoId());
              res.setUserId(comment.getUserId());
              res.setText(comment.getText());
              res.setCreatedAt(comment.getCreatedAt());
              res.setParentCommentId(comment.getParentCommentId());
              res.setLikeCount(comment.getLikeCount());
              res.setReplyCount(comment.getReplyCount());
              if (comment.getUser() != null) {
                res.setUsername(comment.getUser().getUsername());
                res.setUserImageUrl(
                    userAvatarCacheService.getAvatar(comment.getUser().getUserId()));
              }
              return res;
            })
        .toList();
  }

  public PhotoService(
      final CloudinaryService cloudinaryService,
      final PhotoRepository photoRepository,
      final UserService userService,
      final LikeRepository likeRepository,
      final FavoriteRepository favoriteRepository,
      final CommentRepository commentRepository,
      final ShareRepository shareRepository,
      final PhotoConversionService photoConversionService,
      final ApplicationEventPublisher eventPublisher,
      final MongoTemplate mongoTemplate,
      final UserAvatarCacheService userAvatarCacheService) {
    this.cloudinaryService = cloudinaryService;
    this.photoRepository = photoRepository;
    this.userService = userService;
    this.likeRepository = likeRepository;
    this.favoriteRepository = favoriteRepository;
    this.commentRepository = commentRepository;
    this.shareRepository = shareRepository;
    this.photoConversionService = photoConversionService;
    this.eventPublisher = eventPublisher;
    this.mongoTemplate = mongoTemplate;
    this.userAvatarCacheService = userAvatarCacheService;
  }
}
