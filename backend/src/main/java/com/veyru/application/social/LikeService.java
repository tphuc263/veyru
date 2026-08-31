package com.veyru.application.social;

import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.identity.UserProfileService;
import com.veyru.application.notification.NotificationService;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.GraphProjection;
import com.veyru.application.port.out.LikeStore;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.like.LikeResult;
import com.veyru.domain.model.Like;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LikeService {
  private final LikeStore likeStore;
  private final PhotoStore photoStore;
  private final UserStore userStore;
  private final UserProfileService userService;
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
    boolean alreadyLiked = likeStore.exists(photoId, currentUser.id());
    if (alreadyLiked) {
      return;
    }
    likeStore.save(Like.create(photoId, currentUser.id(), clock.instant()));
    photoStore.incrementLikeCount(photoId, 1);
    neo4jGraphService.createLikeRelationship(currentUser.id(), photoId);
    notificationService.sendLikePhotoNotification(
        photo.author().userId(), currentUser, photoId, photo.imageUrl());
  }

  public void unlike(String photoId) {
    User currentUser = userService.requireCurrentUser();
    likeStore
        .find(photoId, currentUser.id())
        .ifPresent(
            like -> {
              likeStore.delete(like);
              photoStore.incrementLikeCount(photoId, -1);
              neo4jGraphService.removeLikeRelationship(currentUser.id(), photoId);
            });
  }

  public List<LikeResult> getPhotoLikes(String photoId) {
    photoStore
        .findById(photoId)
        .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    List<Like> likes = likeStore.findByPhotoId(photoId);
    return convertToLikeResponses(likes);
  }

  public long getPhotoLikesCount(String photoId) {
    return photoStore.findById(photoId).map(Photo::likeCount).orElse(0L);
  }

  private List<LikeResult> convertToLikeResponses(List<Like> likes) {
    List<String> userIds = likes.stream().map(Like::userId).distinct().toList();
    Map<String, User> usersMap =
        userStore.findAllById(userIds).stream().collect(Collectors.toMap(User::id, user -> user));
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

  public LikeService(
      final LikeStore likeStore,
      final PhotoStore photoStore,
      final UserStore userStore,
      final UserProfileService userService,
      final NotificationService notificationService,
      final AvatarCache userAvatarCacheService,
      final GraphProjection neo4jGraphService,
      final Clock clock) {
    this.likeStore = likeStore;
    this.photoStore = photoStore;
    this.userStore = userStore;
    this.userService = userService;
    this.notificationService = notificationService;
    this.userAvatarCacheService = userAvatarCacheService;
    this.neo4jGraphService = neo4jGraphService;
    this.clock = clock;
  }
}
