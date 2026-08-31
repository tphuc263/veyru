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
import com.veyru.domain.enums.UserRole;
import com.veyru.domain.model.Comment;
import com.veyru.domain.model.Like;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.time.Clock;
import java.util.List;
import java.util.Map;

public class PhotoService {
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
    String imageUrl = imageStorage.upload(request.image());
    User currentUser = userService.requireCurrentUser();
    Photo photo =
        Photo.create(
            currentUser.id(),
            currentUser.username(),
            imageUrl,
            request.caption(),
            request.tags(),
            clock.instant());
    Photo savedPhoto = photoStore.save(photo);
    userStore.incrementPhotoCount(currentUser.id(), 1);
    eventPublisher.publish(new PhotoCreatedEvent(savedPhoto.id(), currentUser.id()));
    return photoConversionService.convertToPhotoResponse(
        savedPhoto, PhotoViewer.authenticated(currentUser));
  }

  public PageResult<PhotoResult> getAllPhotos(int page, int size) {
    PageResult<Photo> photos = photoStore.findAll(new PageQuery(page, size));
    PhotoViewer viewer = currentPhotoViewer();
    return photos.map(photo -> photoConversionService.convertToPhotoResponse(photo, viewer));
  }

  public PageResult<PhotoResult> getPhotosByUserId(String userId, int page, int size) {
    PageResult<Photo> photos = photoStore.findByUser(userId, new PageQuery(page, size));
    PhotoViewer viewer = currentPhotoViewer();
    return photos.map(photo -> photoConversionService.convertToPhotoResponse(photo, viewer));
  }

  public PhotoDetailResult getPhotoById(String photoId) {
    Photo photo =
        photoStore
            .findById(photoId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    List<Like> likes = likeStore.findByPhotoId(photoId);
    List<Comment> comments = commentStore.findByPhotoId(photoId);
    PhotoDetailResult response = convertToPhotoDetailResponse(photo);
    response.setLikes(convertToLikeResponses(likes));
    response.setComments(convertToCommentResponses(comments));
    return response;
  }

  public void deletePhoto(String photoId) {
    Photo photo =
        photoStore
            .findById(photoId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    User currentUser = userService.requireCurrentUser();
    if (!photo.author().userId().equals(currentUser.id())
        && currentUser.role() != UserRole.ROLE_ADMIN) {
      throw new UseCaseException(UseCaseError.ACCESS_DENIED);
    }

    imageStorage.deleteByUrl(photo.imageUrl());
    likeStore.deleteAllByPhotoId(photoId);
    commentStore.deleteAllByPhotoId(photoId);
    favoriteStore.deleteAllByPhotoId(photoId);
    shareStore.deleteAllByPhotoId(photoId);
    photoStore.deleteById(photoId);
    userStore.incrementPhotoCount(photo.author().userId(), -1);
  }

  private PhotoDetailResult convertToPhotoDetailResponse(Photo photo) {
    PhotoDetailResult response = new PhotoDetailResult();
    response.setId(photo.id());
    response.setImageUrl(photo.imageUrl());
    response.setCaption(photo.caption());
    response.setCreatedAt(photo.createdAt());
    response.setTags(photo.tags());
    response.setUsername(photo.author().username());
    response.setUserId(photo.author().userId());
    response.setUserImageUrl(userAvatarCacheService.getAvatar(photo.author().userId()));
    response.setLikeCount((int) photo.likeCount());
    response.setCommentCount((int) photo.commentCount());
    response.setShareCount((int) photo.shareCount());

    userService
        .findCurrentUser()
        .ifPresent(
            currentUser -> {
              response.setLikedByCurrentUser(likeStore.exists(photo.id(), currentUser.id()));
              response.setSavedByCurrentUser(favoriteStore.exists(currentUser.id(), photo.id()));
            });

    return response;
  }

  private List<LikeResult> convertToLikeResponses(List<Like> likes) {
    if (likes.isEmpty()) return List.of();
    List<String> userIds = likes.stream().map(Like::userId).distinct().toList();
    Map<String, User> usersMap = userService.findUsersByIds(userIds);
    return likes.stream()
        .map(
            like -> {
              User user = usersMap.get(like.userId());
              return new LikeResult(
                  like.id(),
                  like.userId(),
                  user == null ? null : user.username(),
                  user == null ? null : userAvatarCacheService.getAvatar(user.id()),
                  like.createdAt());
            })
        .toList();
  }

  private List<CommentResult> convertToCommentResponses(List<Comment> comments) {
    if (comments.isEmpty()) return List.of();
    return comments.stream()
        .map(
            comment -> {
              CommentResult res = new CommentResult();
              res.setId(comment.id());
              res.setPhotoId(comment.photoId());
              res.setUserId(comment.userId());
              res.setText(comment.text());
              res.setCreatedAt(comment.createdAt());
              res.setParentCommentId(comment.parentCommentId());
              res.setLikeCount(comment.likeCount());
              res.setReplyCount(comment.replyCount());
              res.setUsername(comment.author().username());
              res.setUserImageUrl(userAvatarCacheService.getAvatar(comment.author().userId()));
              return res;
            })
        .toList();
  }

  private PhotoViewer currentPhotoViewer() {
    return userService
        .findCurrentUser()
        .<PhotoViewer>map(PhotoViewer::authenticated)
        .orElseGet(PhotoViewer::anonymous);
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
