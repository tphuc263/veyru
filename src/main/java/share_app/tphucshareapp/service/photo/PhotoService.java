    package share_app.tphucshareapp.service.photo;

    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
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
    import share_app.tphucshareapp.dto.request.photo.CreatePhotoRequest;
    import share_app.tphucshareapp.dto.response.comment.CommentResponse;
    import share_app.tphucshareapp.dto.response.like.LikeResponse;
    import share_app.tphucshareapp.dto.response.photo.PhotoDetailResponse;
    import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
    import share_app.tphucshareapp.event.PhotoCreatedEvent;
    import share_app.tphucshareapp.model.Comment;
    import share_app.tphucshareapp.model.Like;
    import share_app.tphucshareapp.model.Photo;
    import share_app.tphucshareapp.model.Photo.EmbeddedUser;
    import share_app.tphucshareapp.model.User;
    import share_app.tphucshareapp.repository.CommentRepository;
    import share_app.tphucshareapp.repository.FavoriteRepository;
    import share_app.tphucshareapp.repository.LikeRepository;
    import share_app.tphucshareapp.repository.PhotoRepository;
    import share_app.tphucshareapp.service.tag.TagService;
    import share_app.tphucshareapp.service.user.UserService;

    import java.time.Instant;
    import java.util.List;
    import java.util.Map;

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
        private final TagService tagService;
        private final PhotoConversionService photoConversionService;
        private final ApplicationEventPublisher eventPublisher;
        private final MongoTemplate mongoTemplate;

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
            embeddedUser.setUserImageUrl(currentUser.getImageUrl());

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
                tagService.createOrGetTags(tagNames);
            }

            Photo savedPhoto = photoRepository.save(photo);

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
                log.info("Deleted all likes, comments, and favorites for photo ID: {}", photoId);

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
                response.setUserImageUrl(photo.getUser().getUserImageUrl());
            }
            response.setLikeCount((int) photo.getLikeCount());
            response.setCommentCount((int) photo.getCommentCount());

            try {
                User currentUser = userService.getCurrentUser();
                response.setLikedByCurrentUser(
                        likeRepository.existsByPhotoIdAndUserId(photo.getId(), currentUser.getId())
                );
                response.setSavedByCurrentUser(
                        favoriteRepository.existsByUserIdAndPhotoId(currentUser.getId(), photo.getId())
                );
            } catch (Exception e) {
                response.setLikedByCurrentUser(false);
                response.setSavedByCurrentUser(false);
            }

            return response;
        }

        private List<LikeResponse> convertToLikeResponses(List<Like> likes) {
            if (likes.isEmpty()) return List.of();
            List<String> userIds = likes.stream().map(Like::getUserId).distinct().toList();
            Map<String, User> usersMap = userService.findUsersByIds(userIds);
            return likes.stream().map(like -> {
                LikeResponse res = modelMapper.map(like, LikeResponse.class);
                User user = usersMap.get(like.getUserId());
                if (user != null) {
                    res.setUsername(user.getUsername());
                    res.setUserImageUrl(user.getImageUrl());
                }
                return res;
            }).toList();
        }

        private List<CommentResponse> convertToCommentResponses(List<Comment> comments) {
            if (comments.isEmpty()) return List.of();
            return comments.stream().map(comment -> {
                CommentResponse res = modelMapper.map(comment, CommentResponse.class);
                if (comment.getUser() != null) {
                    res.setUsername(comment.getUser().getUsername());
                    res.setUserImageUrl(comment.getUser().getUserImageUrl());
                }
                return res;
            }).toList();
        }
    }
