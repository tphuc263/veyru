package com.veyru.domain.service.like;

import com.veyru.adapter.in.dto.response.like.LikeResponse;
import com.veyru.adapter.in.dto.response.photo.PhotoResponse;
import com.veyru.domain.model.Like;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import com.veyru.domain.exception.ApiException;
import com.veyru.domain.exception.ErrorCode;
import com.veyru.application.port.out.LikeRepository;
import com.veyru.application.port.out.PhotoRepository;
import com.veyru.application.port.out.UserRepository;
import com.veyru.domain.service.graph.Neo4jGraphService;
import com.veyru.domain.service.notification.NotificationService;
import com.veyru.domain.service.photo.PhotoConversionService;
import com.veyru.domain.service.user.UserAvatarCacheService;
import com.veyru.domain.service.user.UserService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
  private static final Logger log = LoggerFactory.getLogger(LikeService.class);
  private final LikeRepository likeRepository;
  private final PhotoRepository photoRepository;
  private final UserRepository userRepository;
  private final UserService userService;
  private final MongoTemplate mongoTemplate;
  private final PhotoConversionService photoConversionService;
  private final NotificationService notificationService;
  private final UserAvatarCacheService userAvatarCacheService;
  private final Neo4jGraphService neo4jGraphService;

  public PhotoResponse toggleLike(String photoId) {
    User currentUser = userService.getCurrentUser();
    Photo photo =
        photoRepository
            .findById(photoId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    boolean alreadyLiked = likeRepository.existsByPhotoIdAndUserId(photoId, currentUser.getId());
    if (alreadyLiked) {
      // Unlike
      Like like =
          likeRepository
              .findByPhotoIdAndUserId(photoId, currentUser.getId())
              .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
      likeRepository.delete(like);
      Query query = new Query(Criteria.where("_id").is(photoId));
      Update update = new Update().inc("likeCount", -1);
      mongoTemplate.updateFirst(query, update, Photo.class);
      photo.setLikeCount(Math.max(0, photo.getLikeCount() - 1));
      // Sync to Neo4j graph - remove like relationship
      try {
        neo4jGraphService.removeLikeRelationship(currentUser.getId(), photoId);
      } catch (Exception e) {
        log.warn("Failed to sync unlike to Neo4j: {}", e.getMessage());
      }
      log.info("User {} unliked photo {}", currentUser.getId(), photoId);
    } else {
      // Like
      Like like = new Like();
      like.setPhotoId(photoId);
      like.setUserId(currentUser.getId());
      like.setCreatedAt(Instant.now());
      likeRepository.save(like);
      Query query = new Query(Criteria.where("_id").is(photoId));
      Update update = new Update().inc("likeCount", 1);
      mongoTemplate.updateFirst(query, update, Photo.class);
      photo.setLikeCount(photo.getLikeCount() + 1);
      // Sync to Neo4j graph - create like relationship
      try {
        neo4jGraphService.createLikeRelationship(currentUser.getId(), photoId);
      } catch (Exception e) {
        log.warn("Failed to sync like to Neo4j: {}", e.getMessage());
      }
      // Send notification to photo owner
      if (photo.getUser() != null) {
        notificationService.sendLikePhotoNotification(
            photo.getUser().getUserId(), currentUser, photoId, photo.getImageUrl());
      }
      log.info("User {} liked photo {}", currentUser.getId(), photoId);
    }
    // Return full updated photo state - Facebook/Instagram pattern
    return photoConversionService.convertToPhotoResponse(photo, currentUser);
  }

  public void like(String photoId) {
    User currentUser = userService.getCurrentUser();
    Photo photo =
        photoRepository
            .findById(photoId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    boolean alreadyLiked = likeRepository.existsByPhotoIdAndUserId(photoId, currentUser.getId());
    if (alreadyLiked) {
      return;
    }
    Like like = new Like();
    like.setPhotoId(photoId);
    like.setUserId(currentUser.getId());
    like.setCreatedAt(Instant.now());
    likeRepository.save(like);
    Query query = new Query(Criteria.where("_id").is(photoId));
    Update update = new Update().inc("likeCount", 1);
    mongoTemplate.updateFirst(query, update, Photo.class);
    // Sync to Neo4j graph - create like relationship
    try {
      neo4jGraphService.createLikeRelationship(currentUser.getId(), photoId);
    } catch (Exception e) {
      log.warn("Failed to sync like to Neo4j: {}", e.getMessage());
    }
    // Send notification
    if (photo.getUser() != null) {
      notificationService.sendLikePhotoNotification(
          photo.getUser().getUserId(), currentUser, photoId, photo.getImageUrl());
    }
    log.info("User {} liked photo {}", currentUser.getId(), photoId);
  }

  public void unlike(String photoId) {
    User currentUser = userService.getCurrentUser();
    Like like = likeRepository.findByPhotoIdAndUserId(photoId, currentUser.getId()).orElse(null);
    if (like == null) return;
    likeRepository.delete(like);
    Query query = new Query(Criteria.where("_id").is(photoId));
    Update update = new Update().inc("likeCount", -1);
    mongoTemplate.updateFirst(query, update, Photo.class);
    // Sync to Neo4j graph - remove like relationship
    try {
      neo4jGraphService.removeLikeRelationship(currentUser.getId(), photoId);
    } catch (Exception e) {
      log.warn("Failed to sync unlike to Neo4j: {}", e.getMessage());
    }
    log.info("User {} unliked photo {}", currentUser.getId(), photoId);
  }

  public List<LikeResponse> getPhotoLikes(String photoId) {
    // Validate photo exists
    photoRepository
        .findById(photoId)
        .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    List<Like> likes = likeRepository.findByPhotoIdOrderByCreatedAtDesc(photoId);
    return convertToLikeResponses(likes);
  }

  public long getPhotoLikesCount(String photoId) {
    return photoRepository.findById(photoId).map(Photo::getLikeCount).orElse(0L);
  }

  // Helper method
  private List<LikeResponse> convertToLikeResponses(List<Like> likes) {
    // Get all user IDs and fetch users in batch for performance
    List<String> userIds = likes.stream().map(Like::getUserId).distinct().toList();
    Map<String, User> usersMap =
        userRepository.findAllById(userIds).stream()
            .collect(Collectors.toMap(User::getId, user -> user));
    return likes.stream()
        .map(
            like -> {
              LikeResponse response = new LikeResponse();
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
      final LikeRepository likeRepository,
      final PhotoRepository photoRepository,
      final UserRepository userRepository,
      final UserService userService,
      final MongoTemplate mongoTemplate,
      final PhotoConversionService photoConversionService,
      final NotificationService notificationService,
      final UserAvatarCacheService userAvatarCacheService,
      final Neo4jGraphService neo4jGraphService) {
    this.likeRepository = likeRepository;
    this.photoRepository = photoRepository;
    this.userRepository = userRepository;
    this.userService = userService;
    this.mongoTemplate = mongoTemplate;
    this.photoConversionService = photoConversionService;
    this.notificationService = notificationService;
    this.userAvatarCacheService = userAvatarCacheService;
    this.neo4jGraphService = neo4jGraphService;
  }
}
