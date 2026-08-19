package com.veyru.service.share;

import com.veyru.dto.response.photo.PhotoResponse;
import com.veyru.dto.response.share.ShareResponse;
import com.veyru.dto.response.share.ShareWithPhotoResponse;
import com.veyru.model.Photo;
import com.veyru.model.Share;
import com.veyru.model.User;
import com.veyru.repository.PhotoRepository;
import com.veyru.repository.ShareRepository;
import com.veyru.repository.UserRepository;
import com.veyru.service.photo.PhotoConversionService;
import com.veyru.service.user.UserAvatarCacheService;
import com.veyru.service.user.UserService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
import org.springframework.stereotype.Service;

@Service
public class ShareService {
  private static final Logger log = LoggerFactory.getLogger(ShareService.class);
  private final ShareRepository shareRepository;
  private final PhotoRepository photoRepository;
  private final UserRepository userRepository;
  private final UserService userService;
  private final MongoTemplate mongoTemplate;
  private final PhotoConversionService photoConversionService;
  private final UserAvatarCacheService userAvatarCacheService;

  public PhotoResponse sharePhoto(String photoId, String caption) {
    User currentUser = userService.getCurrentUser();
    Photo photo =
        photoRepository
            .findById(photoId)
            .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));
    // Create share record
    Share share = new Share();
    share.setPhotoId(photoId);
    share.setUserId(currentUser.getId());
    share.setCaption(caption);
    share.setCreatedAt(Instant.now());
    shareRepository.save(share);
    // Increment share count on photo
    Query query = new Query(Criteria.where("_id").is(photoId));
    Update update = new Update().inc("shareCount", 1);
    mongoTemplate.updateFirst(query, update, Photo.class);
    photo.setShareCount(photo.getShareCount() + 1);
    log.info("User {} shared photo {} to their profile", currentUser.getId(), photoId);
    return photoConversionService.convertToPhotoResponse(photo, currentUser);
  }

  public List<ShareResponse> getPhotoShares(String photoId) {
    List<Share> shares = shareRepository.findByPhotoIdOrderByCreatedAtDesc(photoId);
    List<String> userIds = shares.stream().map(Share::getUserId).distinct().toList();
    Map<String, User> userMap =
        userRepository.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
    return shares.stream()
        .map(
            share -> {
              ShareResponse response = new ShareResponse();
              response.setId(share.getId());
              response.setPhotoId(share.getPhotoId());
              response.setUserId(share.getUserId());
              response.setCaption(share.getCaption());
              response.setCreatedAt(share.getCreatedAt());
              User user = userMap.get(share.getUserId());
              if (user != null) {
                response.setUsername(user.getUsername());
                response.setUserImageUrl(userAvatarCacheService.getAvatar(user.getId()));
              }
              return response;
            })
        .toList();
  }

  public long getShareCount(String photoId) {
    return shareRepository.countByPhotoId(photoId);
  }

  public boolean hasShared(String photoId) {
    User currentUser = userService.getCurrentUser();
    return shareRepository.existsByPhotoIdAndUserId(photoId, currentUser.getId());
  }

  public Page<ShareWithPhotoResponse> getSharesByUserId(String userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Share> shares = shareRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    // Get all photo IDs from shares
    List<String> photoIds = shares.getContent().stream().map(Share::getPhotoId).distinct().toList();
    // Fetch original photos
    Map<String, Photo> photoMap =
        photoRepository.findAllById(photoIds).stream()
            .collect(Collectors.toMap(Photo::getId, p -> p));
    // Get user info for original photos
    List<String> originalUserIds =
        photoMap.values().stream()
            .map(p -> p.getUser() != null ? p.getUser().getUserId() : null)
            .filter(id -> id != null)
            .distinct()
            .toList();
    Map<String, User> userMap =
        userRepository.findByIdIn(originalUserIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u));
    // Get current sharer user info
    User sharerUser = userRepository.findById(userId).orElse(null);
    return shares.map(
        share -> {
          ShareWithPhotoResponse response = new ShareWithPhotoResponse();
          response.setId(share.getId());
          response.setPhotoId(share.getPhotoId());
          response.setUserId(share.getUserId());
          response.setCaption(share.getCaption());
          response.setCreatedAt(share.getCreatedAt());
          // Set sharer info
          if (sharerUser != null) {
            response.setUsername(sharerUser.getUsername());
            response.setUserImageUrl(userAvatarCacheService.getAvatar(sharerUser.getId()));
          }
          // Set original photo info
          Photo originalPhoto = photoMap.get(share.getPhotoId());
          if (originalPhoto != null) {
            response.setOriginalPhotoId(originalPhoto.getId());
            response.setOriginalImageUrl(originalPhoto.getImageUrl());
            response.setOriginalCaption(originalPhoto.getCaption());
            response.setOriginalCreatedAt(originalPhoto.getCreatedAt());
            response.setOriginalLikeCount((int) originalPhoto.getLikeCount());
            response.setOriginalCommentCount((int) originalPhoto.getCommentCount());
            response.setOriginalShareCount((int) originalPhoto.getShareCount());
            if (originalPhoto.getUser() != null) {
              response.setOriginalUsername(originalPhoto.getUser().getUsername());
              User originalUser = userMap.get(originalPhoto.getUser().getUserId());
              if (originalUser != null) {
                response.setOriginalUserImageUrl(
                    userAvatarCacheService.getAvatar(originalUser.getId()));
              }
            }
          }
          return response;
        });
  }

  /** Get shares by multiple user IDs (for newsfeed) */
  public List<Share> getSharesByUserIds(List<String> userIds) {
    return shareRepository.findByUserIdInOrderByCreatedAtDesc(userIds);
  }

  /** Check if a share is liked by user */
  public boolean isLikedByUser(Share share, User user) {
    return false; // Simplified - shares don't have like feature yet
  }

  public ShareService(
      final ShareRepository shareRepository,
      final PhotoRepository photoRepository,
      final UserRepository userRepository,
      final UserService userService,
      final MongoTemplate mongoTemplate,
      final PhotoConversionService photoConversionService,
      final UserAvatarCacheService userAvatarCacheService) {
    this.shareRepository = shareRepository;
    this.photoRepository = photoRepository;
    this.userRepository = userRepository;
    this.userService = userService;
    this.mongoTemplate = mongoTemplate;
    this.photoConversionService = photoConversionService;
    this.userAvatarCacheService = userAvatarCacheService;
  }
}
