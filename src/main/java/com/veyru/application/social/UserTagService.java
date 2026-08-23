package com.veyru.application.social;

import com.veyru.application.identity.UserProfileService;
import com.veyru.application.notification.NotificationService;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.usertag.UserTagResponse;
import com.veyru.domain.exception.ApiException;
import com.veyru.domain.exception.ErrorCode;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.time.Instant;
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

  public UserTagResponse tagUserInPhoto(String photoId, CreateUserTagCommand request) {
    Photo photo =
        photoStore
            .findById(photoId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    User taggedUser =
        userStore
            .findById(request.taggedUserId())
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    User currentUser = userService.getCurrentUser();
    // Check if only photo owner can tag users
    if (!photo.getUser().getUserId().equals(currentUser.getId())) {
      throw new ApiException(ErrorCode.ACCESS_DENIED);
    }
    // Check if user is already tagged
    if (photo.getUserTags() != null) {
      boolean alreadyTagged =
          photo.getUserTags().stream()
              .anyMatch(t -> t.getTaggedUserId().equals(request.taggedUserId()));
      if (alreadyTagged) {
        throw new ApiException(ErrorCode.RESOURCE_CONFLICT);
      }
    }
    Photo.EmbeddedUserTag embeddedTag =
        new Photo.EmbeddedUserTag(
            request.taggedUserId(),
            currentUser.getId(),
            taggedUser.getUsername(),
            request.positionX(),
            request.positionY(),
            Instant.now());
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
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    User currentUser = userService.getCurrentUser();
    // Check if current user is the photo owner or the tagged user
    boolean isPhotoOwner = photo.getUser().getUserId().equals(currentUser.getId());
    boolean isTaggedUser = taggedUserId.equals(currentUser.getId());
    if (!isPhotoOwner && !isTaggedUser) {
      throw new ApiException(ErrorCode.ACCESS_DENIED);
    }
    // Pull from embedded array
    photoStore.removeUserTag(photoId, taggedUserId);
    log.info(
        "Removed tag of user {} from photo {} by user {}",
        taggedUserId,
        photoId,
        currentUser.getId());
  }

  public List<UserTagResponse> getPhotoUserTags(String photoId) {
    Photo photo =
        photoStore
            .findById(photoId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    if (photo.getUserTags() == null || photo.getUserTags().isEmpty()) {
      return Collections.emptyList();
    }
    return photo.getUserTags().stream().map(tag -> convertToResponse(tag, photoId)).toList();
  }

  public List<UserTagResponse> getPhotosWhereUserIsTagged(String userId) {
    userStore.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
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

  private UserTagResponse convertToResponse(Photo.EmbeddedUserTag tag, String photoId) {
    return UserTagResponse.builder()
        .photoId(photoId)
        .taggedUserId(tag.getTaggedUserId())
        .taggedByUserId(tag.getTaggedByUserId())
        .username(tag.getUsername())
        .userImageUrl(userAvatarCacheService.getAvatar(tag.getTaggedUserId()))
        .positionX(tag.getPositionX())
        .positionY(tag.getPositionY())
        .createdAt(tag.getCreatedAt())
        .build();
  }

  public UserTagService(
      final PhotoStore photoStore,
      final UserStore userStore,
      final UserProfileService userService,
      final NotificationService notificationService,
      final AvatarCache userAvatarCacheService) {
    this.photoStore = photoStore;
    this.userStore = userStore;
    this.userService = userService;
    this.notificationService = notificationService;
    this.userAvatarCacheService = userAvatarCacheService;
  }
}
