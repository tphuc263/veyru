package com.veyru.domain.service.follow;

import com.veyru.adapter.in.dto.response.follow.FollowResponse;
import com.veyru.domain.exception.ApiException;
import com.veyru.domain.exception.ErrorCode;
import com.veyru.domain.model.Follow;
import com.veyru.domain.model.User;
import com.veyru.application.port.out.FollowRepository;
import com.veyru.application.port.out.UserRepository;
import com.veyru.adapter.in.security.userdetails.AppUserDetails;
import com.veyru.domain.service.graph.Neo4jGraphService;
import com.veyru.domain.service.notification.NotificationService;
import com.veyru.domain.service.user.UserAvatarCacheService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class FollowService {
  private static final Logger log = LoggerFactory.getLogger(FollowService.class);
  private final FollowRepository followRepository;
  private final UserRepository userRepository;
  private final MongoTemplate mongoTemplate;
  private final NotificationService notificationService;
  private final UserAvatarCacheService userAvatarCacheService;
  private final Neo4jGraphService neo4jGraphService;

  public void follow(String targetUserId) {
    User currentUser = getCurrentUser();
    Follow existingFollow = checkBeforeFollow(targetUserId, currentUser);
    if (existingFollow != null) return;
    Follow follow = new Follow();
    follow.setFollowerId(currentUser.getId());
    follow.setFollowingId(targetUserId);
    follow.setCreatedAt(Instant.now());
    followRepository.save(follow);
    log.info("User {} followed user {}", currentUser.getId(), targetUserId);
    // Sync to Neo4j graph
    try {
      neo4jGraphService.upsertUser(
          currentUser.getId(),
          currentUser.getUsername(),
          currentUser.getImageUrl(),
          currentUser.getFollowingCount(),
          currentUser.getPhotoCount(),
          currentUser.getBio());
      User targetUser = userRepository.findById(targetUserId).orElse(null);
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
    } catch (Exception e) {
      log.warn("Failed to sync follow to Neo4j: {}", e.getMessage());
    }
    // increase following count of person click follow
    Query followerQuery = new Query(Criteria.where("_id").is(currentUser.getId()));
    Update followerUpdate = new Update().inc("followingCount", 1);
    mongoTemplate.updateFirst(followerQuery, followerUpdate, User.class);
    // increase follower of person who have new follower
    Query followingQuery = new Query(Criteria.where("_id").is(targetUserId));
    Update followingUpdate = new Update().inc("followerCount", 1);
    mongoTemplate.updateFirst(followingQuery, followingUpdate, User.class);
    // Send notification to the user being followed
    notificationService.sendNewFollowerNotification(targetUserId, currentUser);
    log.info("User {} followed user {}", currentUser.getId(), targetUserId);
    // try {
    // newsfeedService.generateNewsfeedCache(currentUser.getId());
    // log.info("Regenerated newsfeed cache after follow for user: {}",
    // currentUser.getId());
    // } catch (Exception e) {
    // log.error("Error regenerating newsfeed cache after follow", e);
    // }
  }

  public void unfollow(String targetUserId) {
    User currentUser = getCurrentUser();
    Follow existingFollow = checkBeforeFollow(targetUserId, currentUser);
    if (existingFollow == null) return;
    followRepository.delete(existingFollow);
    log.info("User {} unfollowed user {}", currentUser.getId(), targetUserId);
    Query followerQuery = new Query(Criteria.where("_id").is(currentUser.getId()));
    Update followerUpdate = new Update().inc("followingCount", -1);
    mongoTemplate.updateFirst(followerQuery, followerUpdate, User.class);
    Query followingQuery = new Query(Criteria.where("_id").is(targetUserId));
    Update followingUpdate = new Update().inc("followerCount", -1);
    mongoTemplate.updateFirst(followingQuery, followingUpdate, User.class);
    // try {
    // newsfeedService.generateNewsfeedCache(currentUser.getId());
    // log.info("Regenerated newsfeed cache after unfollow for user: {}",
    // currentUser.getId());
    // } catch (Exception e) {
    // log.error("Error regenerating newsfeed cache after unfollow", e);
    // }
  }

  public List<FollowResponse> getFollowers(String userId, int page, int size) {
    // Validate user exists
    userRepository
        .findById(userId)
        .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    Pageable pageable = PageRequest.of(page, size);
    Page<Follow> follows = followRepository.findByFollowingIdOrderByCreatedAtDesc(userId, pageable);
    List<String> followerIds = follows.getContent().stream().map(Follow::getFollowerId).toList();
    return convertToFollowResponses(followerIds, true);
  }

  public List<FollowResponse> getFollowing(String userId, int page, int size) {
    // Validate user exists
    userRepository
        .findById(userId)
        .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    Pageable pageable = PageRequest.of(page, size);
    Page<Follow> follows = followRepository.findByFollowerIdOrderByCreatedAtDesc(userId, pageable);
    List<String> followingIds = follows.getContent().stream().map(Follow::getFollowingId).toList();
    return convertToFollowResponses(followingIds, false);
  }

  public boolean isFollowing(String followerId, String followingId) {
    if (followerId == null) followerId = getCurrentUser().getId();
    return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
  }

  // Helper methods
  private List<FollowResponse> convertToFollowResponses(
      List<String> userIds, boolean isFollowersList) {
    if (userIds.isEmpty()) {
      return List.of();
    }
    // Fetch users in batch
    Map<String, User> usersMap =
        userRepository.findAllById(userIds).stream()
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
    try {
      User currentUser = getCurrentUser();
      return followRepository.findByFollowerId(currentUser.getId()).stream()
          .map(Follow::getFollowingId)
          .collect(Collectors.toSet());
    } catch (Exception e) {
      return Set.of();
    }
  }

  private Follow checkBeforeFollow(String targetUserId, User currentUser) {
    userRepository
        .findById(targetUserId)
        .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    // Prevent self-following
    if (currentUser.getId().equals(targetUserId)) {
      throw new ApiException(ErrorCode.VALIDATION_FAILED);
    }
    return followRepository
        .findByFollowerIdAndFollowingId(currentUser.getId(), targetUserId)
        .orElse(null);
  }

  // helper methods
  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || !(authentication.getPrincipal() instanceof AppUserDetails userDetails)) {
      throw new ApiException(ErrorCode.AUTHENTICATION_REQUIRED);
    }
    return userRepository
        .findById(userDetails.getId())
        .orElseThrow(
            () -> {
              log.error("User not found with ID: {}", userDetails.getId());
              return new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
            });
  }

  public FollowService(
      final FollowRepository followRepository,
      final UserRepository userRepository,
      final MongoTemplate mongoTemplate,
      final NotificationService notificationService,
      final UserAvatarCacheService userAvatarCacheService,
      final Neo4jGraphService neo4jGraphService) {
    this.followRepository = followRepository;
    this.userRepository = userRepository;
    this.mongoTemplate = mongoTemplate;
    this.notificationService = notificationService;
    this.userAvatarCacheService = userAvatarCacheService;
    this.neo4jGraphService = neo4jGraphService;
  }
}
