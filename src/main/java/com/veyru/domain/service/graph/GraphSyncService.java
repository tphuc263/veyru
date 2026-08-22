package com.veyru.domain.service.graph;

import com.veyru.application.port.out.FollowRepository;
import com.veyru.application.port.out.LikeRepository;
import com.veyru.application.port.out.PhotoRepository;
import com.veyru.application.port.out.UserRepository;
import com.veyru.domain.model.Follow;
import com.veyru.domain.model.Like;
import com.veyru.domain.model.User;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Graph Sync Service Handles synchronization of data from MongoDB to Neo4j Triggers on: user
 * create, photo create, follow, like, comment
 */
@Service
public class GraphSyncService {
  private static final Logger log = LoggerFactory.getLogger(GraphSyncService.class);
  private final Neo4jGraphService neo4jGraphService;
  private final UserRepository userRepository;
  private final PhotoRepository photoRepository;
  private final FollowRepository followRepository;
  private final LikeRepository likeRepository;

  // ==================== USER OPERATIONS ====================
  /** Sync a user to Neo4j (called when user is created/updated) */
  @Async
  public CompletableFuture<Void> syncUser(String userId) {
    log.info("Syncing user to Neo4j: {}", userId);
    userRepository
        .findById(userId)
        .ifPresent(
            user -> {
              neo4jGraphService.upsertUser(
                  user.getId(),
                  user.getUsername(),
                  user.getImageUrl(),
                  user.getFollowerCount(),
                  user.getPhotoCount(),
                  user.getBio());
            });
    return CompletableFuture.completedFuture(null);
  }

  /** Sync all users to Neo4j (initial sync) */
  public void syncAllUsers() {
    log.info("Starting full user sync to Neo4j");
    List<User> allUsers = userRepository.findAll();
    for (User user : allUsers) {

      neo4jGraphService.upsertUser(
          user.getId(),
          user.getUsername(),
          user.getImageUrl(),
          user.getFollowerCount(),
          user.getPhotoCount(),
          user.getBio());
    }
    log.info("Completed syncing {} users to Neo4j", allUsers.size());
  }

  // ==================== PHOTO OPERATIONS ====================
  /** Sync a photo to Neo4j (called when photo is created) */
  @Async
  public CompletableFuture<Void> syncPhoto(String photoId) {
    log.info("Syncing photo to Neo4j: {}", photoId);
    try {
      photoRepository
          .findById(photoId)
          .ifPresent(
              photo ->
                  neo4jGraphService.upsertPhoto(
                      photo.getId(),
                      photo.getUser().getUserId(),
                      photo.getUser().getUsername(),
                      photo.getImageUrl(),
                      photo.getCaption(),
                      photo.getTags(),
                      photo.getLikeCount(),
                      photo.getCommentCount(),
                      photo.getShareCount(),
                      photo.getCreatedAt()));
    } catch (RuntimeException ex) {
      // ponytail: in-process projection is best-effort; use an outbox when guaranteed delivery matters.
      log.error("Failed to sync photo {} to Neo4j; photo remains available in MongoDB", photoId, ex);
    }
    return CompletableFuture.completedFuture(null);
  }

  /** Sync all photos to Neo4j (initial sync) */
  public void syncAllPhotos() {
    log.info("Starting full photo sync to Neo4j");
    photoRepository
        .findAll()
        .forEach(
            photo -> {
              neo4jGraphService.upsertPhoto(
                  photo.getId(),
                  photo.getUser().getUserId(),
                  photo.getUser().getUsername(),
                  photo.getImageUrl(),
                  photo.getCaption(),
                  photo.getTags(),
                  photo.getLikeCount(),
                  photo.getCommentCount(),
                  photo.getShareCount(),
                  photo.getCreatedAt());
            });
    log.info("Completed syncing photos to Neo4j");
  }

  // ==================== FOLLOW OPERATIONS ====================
  /** Create follow relationship in Neo4j */
  public void createFollow(String followerId, String followingId) {
    log.info("Syncing follow relationship: {} -> {}", followerId, followingId);
    // Ensure both users exist in Neo4j
    syncUser(followerId);
    syncUser(followingId);
    // Create the follow relationship
    neo4jGraphService.createFollowRelationship(followerId, followingId);
  }

  /** Remove follow relationship in Neo4j */
  public void removeFollow(String followerId, String followingId) {
    log.info("Removing follow relationship: {} -> {}", followerId, followingId);
    neo4jGraphService.removeFollowRelationship(followerId, followingId);
  }

  /** Sync all follow relationships (initial sync) */
  public void syncAllFollows() {
    log.info("Starting full follow sync to Neo4j");
    List<Follow> allFollows = followRepository.findAll();
    for (Follow follow : allFollows) {

      createFollow(follow.getFollowerId(), follow.getFollowingId());
    }
    log.info("Completed syncing {} follows to Neo4j", allFollows.size());
  }

  // ==================== LIKE OPERATIONS ====================
  /** Create like relationship in Neo4j */
  public void createLike(String userId, String photoId) {
    log.info("Syncing like relationship: {} -> {}", userId, photoId);
    neo4jGraphService.createLikeRelationship(userId, photoId);
  }

  /** Remove like relationship in Neo4j */
  public void removeLike(String userId, String photoId) {
    log.info("Removing like relationship: {} -> {}", userId, photoId);
    neo4jGraphService.removeLikeRelationship(userId, photoId);
  }

  /** Sync all likes to Neo4j (initial sync) */
  public void syncAllLikes() {
    log.info("Starting full like sync to Neo4j");
    List<Like> allLikes = likeRepository.findAll();
    for (Like like : allLikes) {

      neo4jGraphService.createLikeRelationship(like.getUserId(), like.getPhotoId());
    }
    log.info("Completed syncing {} likes to Neo4j", allLikes.size());
  }

  // ==================== FULL SYNC ====================
  /** Perform full sync of all data to Neo4j Call this once during initial setup */
  public String performFullSync() {
    log.info("Starting full sync to Neo4j graph database");
    long startTime = System.currentTimeMillis();
    // 1. Sync all users
    syncAllUsers();
    // 2. Sync all follows
    syncAllFollows();
    // 3. Sync all photos
    syncAllPhotos();
    // 4. Sync all likes
    syncAllLikes();
    long duration = System.currentTimeMillis() - startTime;
    log.info("Full sync completed in {} ms", duration);
    // Get stats
    var stats = neo4jGraphService.getGraphStats();
    return String.format("Full sync completed in %d ms. Stats: %s", duration, stats);
  }

  /** Get current graph statistics */
  public String getGraphStats() {
    var stats = neo4jGraphService.getGraphStats();
    return String.format(
        "Graph Stats: Users=%d, Photos=%d, Follows=%d, Likes=%d",
        stats.getOrDefault("users", 0L),
        stats.getOrDefault("photos", 0L),
        stats.getOrDefault("follows", 0L),
        stats.getOrDefault("likes", 0L));
  }

  public GraphSyncService(
      final Neo4jGraphService neo4jGraphService,
      final UserRepository userRepository,
      final PhotoRepository photoRepository,
      final FollowRepository followRepository,
      final LikeRepository likeRepository) {
    this.neo4jGraphService = neo4jGraphService;
    this.userRepository = userRepository;
    this.photoRepository = photoRepository;
    this.followRepository = followRepository;
    this.likeRepository = likeRepository;
  }
}
