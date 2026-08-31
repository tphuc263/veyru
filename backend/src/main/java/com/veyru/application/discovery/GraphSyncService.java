package com.veyru.application.discovery;

import com.veyru.application.port.out.FollowStore;
import com.veyru.application.port.out.GraphProjection;
import com.veyru.application.port.out.LikeStore;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.domain.model.Follow;
import com.veyru.domain.model.Like;
import com.veyru.domain.model.User;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Projects Mongo-backed domain state into the disposable Neo4j read model. */
public class GraphSyncService {
  private static final Logger log = LoggerFactory.getLogger(GraphSyncService.class);
  private final GraphProjection neo4jGraphService;
  private final UserStore userStore;
  private final PhotoStore photoStore;
  private final FollowStore followStore;
  private final LikeStore likeStore;

  public CompletableFuture<Void> syncUser(String userId) {
    userStore
        .findById(userId)
        .ifPresent(
            user -> {
              neo4jGraphService.upsertUser(
                  user.id(),
                  user.username(),
                  user.imageUrl(),
                  user.followerCount(),
                  user.photoCount(),
                  user.bio());
            });
    return CompletableFuture.completedFuture(null);
  }

  public void syncAllUsers() {
    List<User> allUsers = userStore.findAll();
    for (User user : allUsers) {
      neo4jGraphService.upsertUser(
          user.id(),
          user.username(),
          user.imageUrl(),
          user.followerCount(),
          user.photoCount(),
          user.bio());
    }
  }

  public CompletableFuture<Void> syncPhoto(String photoId) {
    try {
      photoStore
          .findById(photoId)
          .ifPresent(
              photo -> {
                syncUser(photo.author().userId());
                neo4jGraphService.upsertPhoto(
                    photo.id(),
                    photo.author().userId(),
                    photo.author().username(),
                    photo.imageUrl(),
                    photo.caption(),
                    photo.tags(),
                    photo.likeCount(),
                    photo.commentCount(),
                    photo.shareCount(),
                    photo.createdAt());
              });
    } catch (RuntimeException ex) {
      // This in-process projection is intentionally best-effort; Mongo remains authoritative.
      log.error(
          "Failed to sync photo {} to Neo4j; photo remains available in MongoDB", photoId, ex);
    }
    return CompletableFuture.completedFuture(null);
  }

  public void syncAllPhotos() {
    photoStore
        .findAll()
        .forEach(
            photo -> {
              neo4jGraphService.upsertPhoto(
                  photo.id(),
                  photo.author().userId(),
                  photo.author().username(),
                  photo.imageUrl(),
                  photo.caption(),
                  photo.tags(),
                  photo.likeCount(),
                  photo.commentCount(),
                  photo.shareCount(),
                  photo.createdAt());
            });
  }

  public void createFollow(String followerId, String followingId) {
    syncUser(followerId);
    syncUser(followingId);
    neo4jGraphService.createFollowRelationship(followerId, followingId);
  }

  public void removeFollow(String followerId, String followingId) {
    neo4jGraphService.removeFollowRelationship(followerId, followingId);
  }

  public void syncAllFollows() {
    List<Follow> allFollows = followStore.findAll();
    for (Follow follow : allFollows) {
      createFollow(follow.followerId(), follow.followingId());
    }
  }

  public void createLike(String userId, String photoId) {
    neo4jGraphService.createLikeRelationship(userId, photoId);
  }

  public void removeLike(String userId, String photoId) {
    neo4jGraphService.removeLikeRelationship(userId, photoId);
  }

  public void syncAllLikes() {
    List<Like> allLikes = likeStore.findAll();
    for (Like like : allLikes) {
      neo4jGraphService.createLikeRelationship(like.userId(), like.photoId());
    }
  }

  public String performFullSync() {
    long startTime = System.currentTimeMillis();
    try {
      syncAllUsers();
      syncAllFollows();
      syncAllPhotos();
      syncAllLikes();
      long duration = System.currentTimeMillis() - startTime;
      var stats = neo4jGraphService.getGraphStats();
      log.info(
          "Graph projection rebuild completed: outcome=success, durationMs={}, users={}, photos={}, follows={}, likes={}",
          duration,
          stats.getOrDefault("users", 0L),
          stats.getOrDefault("photos", 0L),
          stats.getOrDefault("follows", 0L),
          stats.getOrDefault("likes", 0L));
      return String.format("Full sync completed in %d ms. Stats: %s", duration, stats);
    } catch (RuntimeException exception) {
      long duration = System.currentTimeMillis() - startTime;
      // Neo4j is a rebuildable projection, so an unavailable graph must not block application
      // startup.
      log.warn(
          "Graph projection rebuild completed: outcome=degraded, durationMs={}",
          duration,
          exception);
      return String.format("Full sync degraded after %d ms", duration);
    }
  }

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
      final GraphProjection neo4jGraphService,
      final UserStore userStore,
      final PhotoStore photoStore,
      final FollowStore followStore,
      final LikeStore likeStore) {
    this.neo4jGraphService = neo4jGraphService;
    this.userStore = userStore;
    this.photoStore = photoStore;
    this.followStore = followStore;
    this.likeStore = likeStore;
  }
}
