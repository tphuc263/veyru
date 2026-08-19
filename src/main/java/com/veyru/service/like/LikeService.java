package com.veyru.service.like;

import com.veyru.dto.response.like.LikeResponse;
import com.veyru.dto.response.photo.PhotoResponse;
import com.veyru.model.Like;
import com.veyru.model.Photo;
import com.veyru.model.User;
import com.veyru.repository.LikeRepository;
import com.veyru.repository.PhotoRepository;
import com.veyru.repository.UserRepository;
import com.veyru.service.graph.Neo4jGraphService;
import com.veyru.service.notification.INotificationService;
import com.veyru.service.photo.PhotoConversionService;
import com.veyru.service.user.UserAvatarCacheService;
import com.veyru.service.user.UserService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService implements ILikeService {
  private final LikeRepository likeRepository;
  private final PhotoRepository photoRepository;
  private final UserRepository userRepository;
  private final UserService userService;
  private final ModelMapper modelMapper;
  private final MongoTemplate mongoTemplate;
  private final PhotoConversionService photoConversionService;
  private final INotificationService notificationService;
  private final UserAvatarCacheService userAvatarCacheService;
  private final Neo4jGraphService neo4jGraphService;

  @Override
  public PhotoResponse toggleLike(String photoId) {
    User currentUser = userService.getCurrentUser();
    Photo photo =
        photoRepository
            .findById(photoId)
            .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

    boolean alreadyLiked = likeRepository.existsByPhotoIdAndUserId(photoId, currentUser.getId());

    if (alreadyLiked) {
      // Unlike
      Like like =
          likeRepository
              .findByPhotoIdAndUserId(photoId, currentUser.getId())
              .orElseThrow(() -> new RuntimeException("Like not found"));
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

  @Override
  public void like(String photoId) {
    User currentUser = userService.getCurrentUser();
    Photo photo =
        photoRepository
            .findById(photoId)
            .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

    boolean alreadyLiked = likeRepository.existsByPhotoIdAndUserId(photoId, currentUser.getId());
    if (alreadyLiked) {
      // Auto-unlike if already liked (toggle behavior)
      unlike(photoId);
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

  @Override
  public void unlike(String photoId) {
    User currentUser = userService.getCurrentUser();
    Like like =
        likeRepository
            .findByPhotoIdAndUserId(photoId, currentUser.getId())
            .orElseThrow(() -> new RuntimeException("You have not liked this photo"));

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

  @Override
  public List<LikeResponse> getPhotoLikes(String photoId) {
    // Validate photo exists
    photoRepository
        .findById(photoId)
        .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

    List<Like> likes = likeRepository.findByPhotoIdOrderByCreatedAtDesc(photoId);
    return convertToLikeResponses(likes);
  }

  @Override
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
              LikeResponse response = modelMapper.map(like, LikeResponse.class);
              User user = usersMap.get(like.getUserId());
              if (user != null) {
                response.setUsername(user.getUsername());
                response.setUserImageUrl(userAvatarCacheService.getAvatar(user.getId()));
              }
              return response;
            })
        .toList();
  }
}
