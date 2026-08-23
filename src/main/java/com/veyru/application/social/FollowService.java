package com.veyru.application.social;

import com.veyru.application.notification.NotificationService;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.CurrentActor;
import com.veyru.application.port.out.FollowStore;
import com.veyru.application.port.out.GraphProjection;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.follow.FollowResponse;
import com.veyru.application.error.ApiException;
import com.veyru.application.error.ErrorCode;
import com.veyru.domain.model.Follow;
import com.veyru.domain.model.User;
import java.time.Instant;
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
  private final CurrentActor currentActor;

  public void follow(String targetUserId) {
    User currentUser = getCurrentUser();
    Follow existingFollow = checkBeforeFollow(targetUserId, currentUser);
    if (existingFollow != null) return;
    Follow follow = Follow.create(currentUser.getId(), targetUserId, Instant.now());
    followStore.save(follow);
    log.info("User {} followed user {}", currentUser.getId(), targetUserId);
    // Sync to Neo4j graph

    neo4jGraphService.upsertUser(
        currentUser.getId(),
        currentUser.getUsername(),
        currentUser.getImageUrl(),
        currentUser.getFollowingCount(),
        currentUser.getPhotoCount(),
        currentUser.getBio());
    User targetUser = userStore.findById(targetUserId).orElse(null);
    if (targetUser != null) {
      neo4jGraphService.upsertUser(
          targetUser.getId(),
          targetUser.getUsername(),
          targetUser.getImageUrl(),
          targetUser.getFollowingCount(),
          targetUser.getPhotoCount(),
          targetUser.getBio());
    }
    neo4jGraphService.createFollowRelationship(currentUser.getId(), targetUserId);
    log.debug("Synced follow relationship to Neo4j: {} -> {}", currentUser.getId(), targetUserId);

    userStore.incrementFollowingCount(currentUser.getId(), 1);
    userStore.incrementFollowerCount(targetUserId, 1);
    // Send notification to the user being followed
    notificationService.sendNewFollowerNotification(targetUserId, currentUser);
    log.info("User {} followed user {}", currentUser.getId(), targetUserId);
    //
    // newsfeedService.generateNewsfeedCache(currentUser.getId());
    // log.info("Regenerated newsfeed cache after follow for user: {}",
    // currentUser.getId());
    //
  }

  public void unfollow(String targetUserId) {
    User currentUser = getCurrentUser();
    Follow existingFollow = checkBeforeFollow(targetUserId, currentUser);
    if (existingFollow == null) return;
    followStore.delete(existingFollow);
    log.info("User {} unfollowed user {}", currentUser.getId(), targetUserId);
    userStore.incrementFollowingCount(currentUser.getId(), -1);
    userStore.incrementFollowerCount(targetUserId, -1);
    //
    // newsfeedService.generateNewsfeedCache(currentUser.getId());
    // log.info("Regenerated newsfeed cache after unfollow for user: {}",
    // currentUser.getId());
    //
  }

  public List<FollowResponse> getFollowers(String userId, int page, int size) {
    // Validate user exists
    userStore.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    List<Follow> follows = followStore.findFollowers(userId, page, size);
    List<String> followerIds = follows.stream().map(Follow::getFollowerId).toList();
    return convertToFollowResponses(followerIds, true);
  }

  public List<FollowResponse> getFollowing(String userId, int page, int size) {
    // Validate user exists
    userStore.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    List<Follow> follows = followStore.findFollowing(userId, page, size);
    List<String> followingIds = follows.stream().map(Follow::getFollowingId).toList();
    return convertToFollowResponses(followingIds, false);
  }

  public boolean isFollowing(String followerId, String followingId) {
    if (followerId == null) followerId = getCurrentUser().getId();
    return followStore.exists(followerId, followingId);
  }

  // Helper methods
  private List<FollowResponse> convertToFollowResponses(
      List<String> userIds, boolean isFollowersList) {
    if (userIds.isEmpty()) {
      return List.of();
    }
    // Fetch users in batch
    Map<String, User> usersMap =
        userStore.findAllById(userIds).stream()
            .collect(Collectors.toMap(User::getId, user -> user));
    // Get current user's following list for follow status
    Set<String> currentUserFollowing = getCurrentUserFollowing();
    return userIds.stream()
        .map(
            userId -> {
              User user = usersMap.get(userId);
              if (user != null) {
                FollowResponse response = new FollowResponse();
                response.setId(user.getId());
                response.setUsername(user.getUsername());
                response.setBio(user.getBio());
                response.setUserId(user.getId());
                response.setUserImageUrl(userAvatarCacheService.getAvatar(user.getId()));
                response.setFollowedByCurrentUser(currentUserFollowing.contains(userId));
                return response;
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
                    .map(Follow::getFollowingId)
                    .collect(Collectors.toSet()))
        .orElseGet(Set::of);
  }

  private Follow checkBeforeFollow(String targetUserId, User currentUser) {
    userStore
        .findById(targetUserId)
        .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    // Prevent self-following
    if (currentUser.getId().equals(targetUserId)) {
      throw new ApiException(ErrorCode.VALIDATION_FAILED);
    }
    return followStore.find(currentUser.getId(), targetUserId).orElse(null);
  }

  // helper methods
  private User getCurrentUser() {
    String actorId =
        currentActor.id().orElseThrow(() -> new ApiException(ErrorCode.AUTHENTICATION_REQUIRED));
    return userStore
        .findById(actorId)
        .orElseThrow(
            () -> {
              log.error("User not found with ID: {}", actorId);
              return new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
            });
  }

  public FollowService(
      final FollowStore followStore,
      final UserStore userStore,
      final NotificationService notificationService,
      final AvatarCache userAvatarCacheService,
      final GraphProjection neo4jGraphService,
      final CurrentActor currentActor) {
    this.followStore = followStore;
    this.userStore = userStore;
    this.notificationService = notificationService;
    this.userAvatarCacheService = userAvatarCacheService;
    this.neo4jGraphService = neo4jGraphService;
    this.currentActor = currentActor;
  }
}
