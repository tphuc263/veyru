package com.veyru.application.social;

import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.identity.UserProfileService;
import com.veyru.application.notification.NotificationService;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.usertag.UserTagResult;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.PhotoUserTag;
import com.veyru.domain.model.User;
import java.time.Clock;
import java.util.List;

public class UserTagService {
  private final PhotoStore photoStore;
  private final UserStore userStore;
  private final UserProfileService userService;
  private final NotificationService notificationService;
  private final AvatarCache userAvatarCacheService;
  private final Clock clock;

  public UserTagResult tagUserInPhoto(String photoId, CreateUserTagCommand request) {
    Photo photo =
        photoStore
            .findById(photoId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    User taggedUser =
        userStore
            .findById(request.taggedUserId())
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    User currentUser = userService.requireCurrentUser();
    if (!photo.author().userId().equals(currentUser.id())) {
      throw new UseCaseException(UseCaseError.ACCESS_DENIED);
    }
    boolean alreadyTagged =
        photo.userTags().stream()
            .anyMatch(tag -> tag.taggedUserId().equals(request.taggedUserId()));
    if (alreadyTagged) {
      throw new UseCaseException(UseCaseError.RESOURCE_CONFLICT);
    }
    PhotoUserTag embeddedTag =
        new PhotoUserTag(
            request.taggedUserId(),
            currentUser.id(),
            taggedUser.username(),
            request.positionX(),
            request.positionY(),
            clock.instant());
    photoStore.addUserTag(photoId, embeddedTag);
    notificationService.sendTagInPhotoNotification(
        request.taggedUserId(), currentUser, photoId, photo.imageUrl());
    return convertToResponse(embeddedTag, photoId);
  }

  public void removeUserTag(String photoId, String taggedUserId) {
    Photo photo =
        photoStore
            .findById(photoId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    User currentUser = userService.requireCurrentUser();
    boolean isPhotoOwner = photo.author().userId().equals(currentUser.id());
    boolean isTaggedUser = taggedUserId.equals(currentUser.id());
    if (!isPhotoOwner && !isTaggedUser) {
      throw new UseCaseException(UseCaseError.ACCESS_DENIED);
    }
    photoStore.removeUserTag(photoId, taggedUserId);
  }

  public List<UserTagResult> getPhotoUserTags(String photoId) {
    Photo photo =
        photoStore
            .findById(photoId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    return photo.userTags().stream().map(tag -> convertToResponse(tag, photoId)).toList();
  }

  public List<UserTagResult> getPhotosWhereUserIsTagged(String userId) {
    userStore
        .findById(userId)
        .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    List<Photo> photos = photoStore.findTaggedUser(userId);
    return photos.stream()
        .flatMap(
            photo -> {
              return photo.userTags().stream()
                  .filter(tag -> userId.equals(tag.taggedUserId()))
                  .map(tag -> convertToResponse(tag, photo.id()));
            })
        .toList();
  }

  private UserTagResult convertToResponse(PhotoUserTag tag, String photoId) {
    return new UserTagResult(
        null,
        photoId,
        tag.taggedUserId(),
        tag.taggedByUserId(),
        tag.username(),
        userAvatarCacheService.getAvatar(tag.taggedUserId()),
        tag.positionX(),
        tag.positionY(),
        tag.createdAt());
  }

  public UserTagService(
      final PhotoStore photoStore,
      final UserStore userStore,
      final UserProfileService userService,
      final NotificationService notificationService,
      final AvatarCache userAvatarCacheService,
      final Clock clock) {
    this.photoStore = photoStore;
    this.userStore = userStore;
    this.userService = userService;
    this.notificationService = notificationService;
    this.userAvatarCacheService = userAvatarCacheService;
    this.clock = clock;
  }
}
