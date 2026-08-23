package com.veyru.application.media;

import com.veyru.application.common.PageQuery;
import com.veyru.application.common.PageResult;
import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.event.PhotoCreatedEvent;
import com.veyru.application.identity.UserProfileService;
import com.veyru.application.port.out.*;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.ImageStorage;
import com.veyru.application.result.comment.CommentResult;
import com.veyru.application.result.like.LikeResult;
import com.veyru.application.result.photo.PhotoDetailResult;
import com.veyru.application.result.photo.PhotoResult;
import com.veyru.domain.model.Comment;
import com.veyru.domain.model.Like;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhotoService {
  private static final Logger log = LoggerFactory.getLogger(PhotoService.class);
  private final ImageStorage imageStorage;
  private final PhotoStore photoStore;
  private final UserStore userStore;
  private final UserProfileService userService;
  private final LikeStore likeStore;
  private final FavoriteStore favoriteStore;
  private final CommentStore commentStore;
  private final ShareStore shareStore;
  private final PhotoConversionService photoConversionService;
  private final PhotoCreatedEventPublisher eventPublisher;
  private final AvatarCache userAvatarCacheService;
  private final Clock clock;

  public PhotoResult createPhoto(CreatePhotoCommand request) {
    log.info("Creating new photo with caption: {}", request.caption());
    String imageUrl = imageStorage.upload(request.image());
    log.info("Image uploaded successfully, URL: {}", imageUrl);
    // Create photo entity
    User currentUser = userService.requireCurrentUser();
    Photo photo =
        Photo.create(
            currentUser.getId(),
            currentUser.getUsername(),
            imageUrl,
            request.caption(),
            request.tags(),
            clock.instant());
    Photo savedPhoto = photoStore.save(photo);
    userStore.incrementPhotoCount(currentUser.getId(), 1);
    // Publish event to update followers' feeds asynchronously
    eventPublisher.publish(new PhotoCreatedEvent(savedPhoto.getId(), currentUser.getId()));
    log.info("Photo created successfully with ID: {}", savedPhoto.getId());
    return photoConversionService.convertToPhotoResponse(savedPhoto, Optional.of(currentUser));
  }

  public PageResult<PhotoResult> getAllPhotos(int page, int size) {
    log.info("Fetching all photos - page: {}, size: {}", page, size);
    PageResult<Photo> photos = photoStore.findAll(new PageQuery(page, size));
    Optional<User> currentUser = userService.findCurrentUser();
    return photos.map(photo -> photoConversionService.convertToPhotoResponse(photo, currentUser));
  }

  public PageResult<PhotoResult> getPhotosByUserId(String userId, int page, int size) {
    log.info("Fetching photos for user ID: {} - page: {}, size: {}", userId, page, size);
    PageResult<Photo> photos = photoStore.findByUser(userId, new PageQuery(page, size));
    Optional<User> currentUser = userService.findCurrentUser();
    return photos.map(photo -> photoConversionService.convertToPhotoResponse(photo, currentUser));
  }

  public PhotoDetailResult getPhotoById(String photoId) {
    log.info("Fetching photo details for ID: {}", photoId);
    Photo photo =
        photoStore
            .findById(photoId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    List<Like> likes = likeStore.findByPhotoId(photoId);
    List<Comment> comments = commentStore.findByPhotoId(photoId);
    // Convert to response
    PhotoDetailResult response = convertToPhotoDetailResponse(photo);
    response.setLikes(convertToLikeResponses(likes));
    response.setComments(convertToCommentResponses(comments));
    return response;
  }

  public void deletePhoto(String photoId) {
    log.info("Deleting photo with ID: {}", photoId);
    // Verify photo exists and user has permission to delete
    Photo photo =
        photoStore
            .findById(photoId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    User currentUser = userService.requireCurrentUser();
    // Check if current user is the owner of the photo or admin
    if (!photo.getUser().getUserId().equals(currentUser.getId())
        && !currentUser.getRole().name().equals("ROLE_ADMIN")) {
      throw new UseCaseException(UseCaseError.ACCESS_DENIED);
    }

    // Delete image from Cloudinary
    imageStorage.deleteByUrl(photo.getImageUrl());
    likeStore.deleteAllByPhotoId(photoId);
    commentStore.deleteAllByPhotoId(photoId);
    favoriteStore.deleteAllByPhotoId(photoId);
    shareStore.deleteAllByPhotoId(photoId);
    log.info("Deleted all likes, comments, favorites and shares for photo ID: {}", photoId);
    photoStore.deleteById(photoId);
    userStore.incrementPhotoCount(photo.getUser().getUserId(), -1);
    log.info("Photo deleted successfully with ID: {}", photoId);
  }

  // Keep these methods for PhotoDetailResult (specific to this service)
  private PhotoDetailResult convertToPhotoDetailResponse(Photo photo) {
    PhotoDetailResult response = new PhotoDetailResult();
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

    userService
        .findCurrentUser()
        .ifPresent(
            currentUser -> {
              response.setLikedByCurrentUser(likeStore.exists(photo.getId(), currentUser.getId()));
              response.setSavedByCurrentUser(
                  favoriteStore.exists(currentUser.getId(), photo.getId()));
            });

    return response;
  }

  private List<LikeResult> convertToLikeResponses(List<Like> likes) {
    if (likes.isEmpty()) return List.of();
    List<String> userIds = likes.stream().map(Like::getUserId).distinct().toList();
    Map<String, User> usersMap = userService.findUsersByIds(userIds);
    return likes.stream()
        .map(
            like -> {
              User user = usersMap.get(like.getUserId());
              return new LikeResult(
                  like.getId(),
                  like.getUserId(),
                  user == null ? null : user.getUsername(),
                  user == null ? null : userAvatarCacheService.getAvatar(user.getId()),
                  like.getCreatedAt());
            })
        .toList();
  }

  private List<CommentResult> convertToCommentResponses(List<Comment> comments) {
    if (comments.isEmpty()) return List.of();
    return comments.stream()
        .map(
            comment -> {
              CommentResult res = new CommentResult();
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
      final ImageStorage imageStorage,
      final PhotoStore photoStore,
      final UserStore userStore,
      final UserProfileService userService,
      final LikeStore likeStore,
      final FavoriteStore favoriteStore,
      final CommentStore commentStore,
      final ShareStore shareStore,
      final PhotoConversionService photoConversionService,
      final PhotoCreatedEventPublisher eventPublisher,
      final AvatarCache userAvatarCacheService,
      final Clock clock) {
    this.imageStorage = imageStorage;
    this.photoStore = photoStore;
    this.userStore = userStore;
    this.userService = userService;
    this.likeStore = likeStore;
    this.favoriteStore = favoriteStore;
    this.commentStore = commentStore;
    this.shareStore = shareStore;
    this.photoConversionService = photoConversionService;
    this.eventPublisher = eventPublisher;
    this.userAvatarCacheService = userAvatarCacheService;
    this.clock = clock;
  }
}
