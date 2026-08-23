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
import com.veyru.domain.model.User;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserTagService {
  private static final Logger log = LoggerFactory.getLogger(UserTagService.class);
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
    // Check if only photo owner can tag users
    if (!photo.getUser().getUserId().equals(currentUser.getId())) {
      throw new UseCaseException(UseCaseError.ACCESS_DENIED);
    }
    // Check if user is already tagged
    if (photo.getUserTags() != null) {
      boolean alreadyTagged =
          photo.getUserTags().stream()
              .anyMatch(t -> t.getTaggedUserId().equals(request.taggedUserId()));
      if (alreadyTagged) {
        throw new UseCaseException(UseCaseError.RESOURCE_CONFLICT);
      }
    }
    Photo.EmbeddedUserTag embeddedTag =
        new Photo.EmbeddedUserTag(
            request.taggedUserId(),
            currentUser.getId(),
            taggedUser.getUsername(),
            request.positionX(),
            request.positionY(),
            clock.instant());
    // Push to embedded array
    photoStore.addUserTag(photoId, embeddedTag);
    // Send notification to tagged user
    notificationService.sendTagInPhotoNotification(
        request.taggedUserId(), currentUser, photoId, photo.getImageUrl());
    log.info(
        "User {} tagged user {} in photo {}", currentUser.getId(), request.taggedUserId(), photoId);
    return convertToResponse(embeddedTag, photoId);
  }

  public void removeUserTag(String photoId, String taggedUserId) {
    Photo photo =
        photoStore
            .findById(photoId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    User currentUser = userService.requireCurrentUser();
    // Check if current user is the photo owner or the tagged user
    boolean isPhotoOwner = photo.getUser().getUserId().equals(currentUser.getId());
    boolean isTaggedUser = taggedUserId.equals(currentUser.getId());
    if (!isPhotoOwner && !isTaggedUser) {
      throw new UseCaseException(UseCaseError.ACCESS_DENIED);
    }
    // Pull from embedded array
    photoStore.removeUserTag(photoId, taggedUserId);
    log.info(
        "Removed tag of user {} from photo {} by user {}",
        taggedUserId,
        photoId,
        currentUser.getId());
  }

  public List<UserTagResult> getPhotoUserTags(String photoId) {
    Photo photo =
        photoStore
            .findById(photoId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    if (photo.getUserTags() == null || photo.getUserTags().isEmpty()) {
      return Collections.emptyList();
    }
    return photo.getUserTags().stream().map(tag -> convertToResponse(tag, photoId)).toList();
  }

  public List<UserTagResult> getPhotosWhereUserIsTagged(String userId) {
    userStore
        .findById(userId)
        .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    // Query photos where userTags array contains an element with matching taggedUserId
    List<Photo> photos = photoStore.findTaggedUser(userId);
    return photos.stream()
        .flatMap(
            photo -> {
              if (photo.getUserTags() == null) return java.util.stream.Stream.empty();
              return photo.getUserTags().stream()
                  .filter(tag -> userId.equals(tag.getTaggedUserId()))
                  .map(tag -> convertToResponse(tag, photo.getId()));
            })
        .toList();
  }

  private UserTagResult convertToResponse(Photo.EmbeddedUserTag tag, String photoId) {
    return new UserTagResult(
        null,
        photoId,
        tag.getTaggedUserId(),
        tag.getTaggedByUserId(),
        tag.getUsername(),
        userAvatarCacheService.getAvatar(tag.getTaggedUserId()),
        tag.getPositionX(),
        tag.getPositionY(),
        tag.getCreatedAt());
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
