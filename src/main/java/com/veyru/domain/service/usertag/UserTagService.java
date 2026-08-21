package com.veyru.domain.service.usertag;

import com.veyru.adapter.in.dto.request.usertag.CreateUserTagRequest;
import com.veyru.adapter.in.dto.response.usertag.UserTagResponse;
import com.veyru.domain.exception.ApiException;
import com.veyru.domain.exception.ErrorCode;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import com.veyru.application.port.out.PhotoRepository;
import com.veyru.application.port.out.UserRepository;
import com.veyru.domain.service.notification.NotificationService;
import com.veyru.domain.service.user.UserAvatarCacheService;
import com.veyru.domain.service.user.UserService;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class UserTagService {
  private static final Logger log = LoggerFactory.getLogger(UserTagService.class);
  private final PhotoRepository photoRepository;
  private final UserRepository userRepository;
  private final UserService userService;
  private final NotificationService notificationService;
  private final UserAvatarCacheService userAvatarCacheService;
  private final MongoTemplate mongoTemplate;

  public UserTagResponse tagUserInPhoto(String photoId, CreateUserTagRequest request) {
    Photo photo =
        photoRepository
            .findById(photoId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    User taggedUser =
        userRepository
            .findById(request.getTaggedUserId())
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
              .anyMatch(t -> t.getTaggedUserId().equals(request.getTaggedUserId()));
      if (alreadyTagged) {
        throw new ApiException(ErrorCode.RESOURCE_CONFLICT);
      }
    }
    Photo.EmbeddedUserTag embeddedTag =
        new Photo.EmbeddedUserTag(
            request.getTaggedUserId(),
            currentUser.getId(),
            taggedUser.getUsername(),
            request.getPositionX(),
            request.getPositionY(),
            Instant.now());
    // Push to embedded array
    Query query = new Query(Criteria.where("_id").is(photoId));
    Update update = new Update().push("userTags", embeddedTag);
    mongoTemplate.updateFirst(query, update, Photo.class);
    // Send notification to tagged user
    notificationService.sendTagInPhotoNotification(
        request.getTaggedUserId(), currentUser, photoId, photo.getImageUrl());
    log.info(
        "User {} tagged user {} in photo {}",
        currentUser.getId(),
        request.getTaggedUserId(),
        photoId);
    return convertToResponse(embeddedTag, photoId);
  }

  public void removeUserTag(String photoId, String taggedUserId) {
    Photo photo =
        photoRepository
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
    Query query = new Query(Criteria.where("_id").is(photoId));
    Update update =
        new Update().pull("userTags", new org.bson.Document("taggedUserId", taggedUserId));
    mongoTemplate.updateFirst(query, update, Photo.class);
    log.info(
        "Removed tag of user {} from photo {} by user {}",
        taggedUserId,
        photoId,
        currentUser.getId());
  }

  public List<UserTagResponse> getPhotoUserTags(String photoId) {
    Photo photo =
        photoRepository
            .findById(photoId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    if (photo.getUserTags() == null || photo.getUserTags().isEmpty()) {
      return Collections.emptyList();
    }
    return photo.getUserTags().stream().map(tag -> convertToResponse(tag, photoId)).toList();
  }

  public List<UserTagResponse> getPhotosWhereUserIsTagged(String userId) {
    userRepository
        .findById(userId)
        .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    // Query photos where userTags array contains an element with matching taggedUserId
    Query query = new Query(Criteria.where("userTags.taggedUserId").is(userId));
    List<Photo> photos = mongoTemplate.find(query, Photo.class);
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
      final PhotoRepository photoRepository,
      final UserRepository userRepository,
      final UserService userService,
      final NotificationService notificationService,
      final UserAvatarCacheService userAvatarCacheService,
      final MongoTemplate mongoTemplate) {
    this.photoRepository = photoRepository;
    this.userRepository = userRepository;
    this.userService = userService;
    this.notificationService = notificationService;
    this.userAvatarCacheService = userAvatarCacheService;
    this.mongoTemplate = mongoTemplate;
  }
}
