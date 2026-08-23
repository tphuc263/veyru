package com.veyru.application.social;

import com.veyru.application.identity.UserProfileService;
import com.veyru.application.media.PhotoConversionService;
import com.veyru.application.notification.NotificationService;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.GraphProjection;
import com.veyru.application.port.out.LikeStore;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.like.LikeResult;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.common.error.UseCaseError;
import com.veyru.domain.model.Like;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.time.Instant;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LikeService {
  private static final Logger log = LoggerFactory.getLogger(LikeService.class);
  private final LikeStore likeStore;
  private final PhotoStore photoStore;
  private final UserStore userStore;
  private final UserProfileService userService;
  private final PhotoConversionService photoConversionService;
  private final NotificationService notificationService;
  private final AvatarCache userAvatarCacheService;
  private final GraphProjection neo4jGraphService;
  private final Clock clock;

  public void like(String photoId) {
    User currentUser = userService.requireCurrentUser();
    Photo photo =
        photoStore
            .findById(photoId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    boolean alreadyLiked = likeStore.exists(photoId, currentUser.getId());
    if (alreadyLiked) {
      return;
    }
    Like like = new Like();
    like.setPhotoId(photoId);
    like.setUserId(currentUser.getId());
    like.setCreatedAt(clock.instant());
    likeStore.save(like);
    photoStore.incrementLikeCount(photoId, 1);
    // Sync to Neo4j graph - create like relationship

    neo4jGraphService.createLikeRelationship(currentUser.getId(), photoId);

    // Send notification
    if (photo.getUser() != null) {
      notificationService.sendLikePhotoNotification(
          photo.getUser().getUserId(), currentUser, photoId, photo.getImageUrl());
    }
    log.info("User {} liked photo {}", currentUser.getId(), photoId);
  }

  public void unlike(String photoId) {
    User currentUser = userService.requireCurrentUser();
    Like like = likeStore.find(photoId, currentUser.getId()).orElse(null);
    if (like == null) return;
    likeStore.delete(like);
    photoStore.incrementLikeCount(photoId, -1);
    // Sync to Neo4j graph - remove like relationship

    neo4jGraphService.removeLikeRelationship(currentUser.getId(), photoId);

    log.info("User {} unliked photo {}", currentUser.getId(), photoId);
  }

  public List<LikeResult> getPhotoLikes(String photoId) {
    // Validate photo exists
    photoStore.findById(photoId).orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    List<Like> likes = likeStore.findByPhotoId(photoId);
    return convertToLikeResponses(likes);
  }

  public long getPhotoLikesCount(String photoId) {
    return photoStore.findById(photoId).map(Photo::getLikeCount).orElse(0L);
  }

  // Helper method
  private List<LikeResult> convertToLikeResponses(List<Like> likes) {
    // Get all user IDs and fetch users in batch for performance
    List<String> userIds = likes.stream().map(Like::getUserId).distinct().toList();
    Map<String, User> usersMap =
        userStore.findAllById(userIds).stream()
            .collect(Collectors.toMap(User::getId, user -> user));
    return likes.stream()
        .map(
            like -> {
              LikeResult response = new LikeResult();
              response.setId(like.getId());
              response.setUserId(like.getUserId());
              response.setCreatedAt(like.getCreatedAt());
              User user = usersMap.get(like.getUserId());
              if (user != null) {
                response.setUsername(user.getUsername());
                response.setUserImageUrl(userAvatarCacheService.getAvatar(user.getId()));
              }
              return response;
            })
        .toList();
  }

  public LikeService(
      final LikeStore likeStore,
      final PhotoStore photoStore,
      final UserStore userStore,
      final UserProfileService userService,
      final PhotoConversionService photoConversionService,
      final NotificationService notificationService,
      final AvatarCache userAvatarCacheService,
      final GraphProjection neo4jGraphService,
      final Clock clock) {
    this.likeStore = likeStore;
    this.photoStore = photoStore;
    this.userStore = userStore;
    this.userService = userService;
    this.photoConversionService = photoConversionService;
    this.notificationService = notificationService;
    this.userAvatarCacheService = userAvatarCacheService;
    this.neo4jGraphService = neo4jGraphService;
    this.clock = clock;
  }
}
