package com.veyru.application.social;

import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.notification.NotificationService;
import com.veyru.application.port.out.AffinityCache;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.CurrentActor;
import com.veyru.application.port.out.FollowStore;
import com.veyru.application.port.out.GraphProjection;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.follow.FollowResult;
import com.veyru.domain.model.Follow;
import com.veyru.domain.model.User;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FollowService {
  private static final Logger log = LoggerFactory.getLogger(FollowService.class);
  private final FollowStore followStore;
  private final UserStore userStore;
  private final NotificationService notificationService;
  private final AvatarCache userAvatarCacheService;
  private final GraphProjection neo4jGraphService;
  private final AffinityCache affinityCache;
  private final CurrentActor currentActor;
  private final Clock clock;

  public void follow(String targetUserId) {
    User currentUser = getCurrentUser();
    Follow follow = Follow.create(currentUser.id(), targetUserId, clock.instant());
    requireUser(targetUserId);
    if (followStore.find(currentUser.id(), targetUserId).isPresent()) return;
    followStore.save(follow);
    userStore.incrementFollowingCount(currentUser.id(), 1);
    userStore.incrementFollowerCount(targetUserId, 1);
    neo4jGraphService.upsertUser(
        currentUser.id(),
        currentUser.username(),
        currentUser.imageUrl(),
        currentUser.followerCount(),
        currentUser.photoCount(),
        currentUser.bio());
    User updatedTarget = requireUser(targetUserId);
    neo4jGraphService.upsertUser(
        updatedTarget.id(),
        updatedTarget.username(),
        updatedTarget.imageUrl(),
        updatedTarget.followerCount(),
        updatedTarget.photoCount(),
        updatedTarget.bio());
    neo4jGraphService.createFollowRelationship(currentUser.id(), targetUserId);
    evictAffinity(currentUser.id());
    notificationService.sendNewFollowerNotification(targetUserId, currentUser);
  }

  public void unfollow(String targetUserId) {
    User currentUser = getCurrentUser();
    requireUser(targetUserId);
    followStore
        .find(currentUser.id(), targetUserId)
        .ifPresent(
            existingFollow -> {
              followStore.delete(existingFollow);
              userStore.incrementFollowingCount(currentUser.id(), -1);
              userStore.incrementFollowerCount(targetUserId, -1);
              neo4jGraphService.removeFollowRelationship(currentUser.id(), targetUserId);
              userStore
                  .findById(targetUserId)
                  .ifPresent(
                      targetUser ->
                          neo4jGraphService.upsertUser(
                              targetUser.id(),
                              targetUser.username(),
                              targetUser.imageUrl(),
                              targetUser.followerCount(),
                              targetUser.photoCount(),
                              targetUser.bio()));
              evictAffinity(currentUser.id());
            });
  }

  public List<FollowResult> getFollowers(String userId, int page, int size) {
    userStore
        .findById(userId)
        .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    List<Follow> follows = followStore.findFollowers(userId, page, size);
    List<String> followerIds = follows.stream().map(Follow::followerId).toList();
    return convertToFollowResponses(followerIds);
  }

  public List<FollowResult> getFollowing(String userId, int page, int size) {
    userStore
        .findById(userId)
        .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    List<Follow> follows = followStore.findFollowing(userId, page, size);
    List<String> followingIds = follows.stream().map(Follow::followingId).toList();
    return convertToFollowResponses(followingIds);
  }

  public boolean isFollowing(String followerId, String followingId) {
    if (followerId == null) followerId = getCurrentUser().id();
    return followStore.exists(followerId, followingId);
  }

  private List<FollowResult> convertToFollowResponses(List<String> userIds) {
    if (userIds.isEmpty()) {
      return List.of();
    }
    Map<String, User> usersMap =
        userStore.findAllById(userIds).stream().collect(Collectors.toMap(User::id, user -> user));
    Set<String> currentUserFollowing = getCurrentUserFollowing();
    return userIds.stream()
        .map(
            userId -> {
              User user = usersMap.get(userId);
              if (user != null) {
                return new FollowResult(
                    user.id(),
                    user.id(),
                    user.username(),
                    userAvatarCacheService.getAvatar(user.id()),
                    null,
                    null,
                    user.bio(),
                    currentUserFollowing.contains(userId));
              }
              return null;
            })
        .filter(Objects::nonNull)
        .toList();
  }

  private Set<String> getCurrentUserFollowing() {
    return currentActor
        .id()
        .map(
            actorId ->
                followStore.findByFollowerId(actorId).stream()
                    .map(Follow::followingId)
                    .collect(Collectors.toSet()))
        .orElseGet(Set::of);
  }

  private User requireUser(String userId) {
    return userStore
        .findById(userId)
        .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
  }

  private User getCurrentUser() {
    String actorId =
        currentActor
            .id()
            .orElseThrow(() -> new UseCaseException(UseCaseError.AUTHENTICATION_REQUIRED));
    return userStore
        .findById(actorId)
        .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
  }

  private void evictAffinity(String userId) {
    try {
      affinityCache.evict(userId);
    } catch (RuntimeException exception) {
      log.warn("Could not evict affinity cache for user {}", userId, exception);
    }
  }

  public FollowService(
      final FollowStore followStore,
      final UserStore userStore,
      final NotificationService notificationService,
      final AvatarCache userAvatarCacheService,
      final GraphProjection neo4jGraphService,
      final AffinityCache affinityCache,
      final CurrentActor currentActor,
      final Clock clock) {
    this.followStore = followStore;
    this.userStore = userStore;
    this.notificationService = notificationService;
    this.userAvatarCacheService = userAvatarCacheService;
    this.neo4jGraphService = neo4jGraphService;
    this.affinityCache = affinityCache;
    this.currentActor = currentActor;
    this.clock = clock;
  }
}
