package com.veyru.application.social;

import com.veyru.application.common.PageResult;
import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.identity.UserProfileService;
import com.veyru.application.media.PhotoConversionService;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.ShareStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.photo.PhotoResult;
import com.veyru.application.result.share.ShareResult;
import com.veyru.application.result.share.ShareWithPhotoResult;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.Share;
import com.veyru.domain.model.User;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShareService {
  private static final Logger log = LoggerFactory.getLogger(ShareService.class);
  private final ShareStore shareStore;
  private final PhotoStore photoStore;
  private final UserStore userStore;
  private final UserProfileService userService;
  private final PhotoConversionService photoConversionService;
  private final AvatarCache userAvatarCacheService;
  private final Clock clock;

  public PhotoResult sharePhoto(String photoId, String caption) {
    User currentUser = userService.requireCurrentUser();
    Photo photo =
        photoStore
            .findById(photoId)
            .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    // Create share record
    Share share = new Share();
    share.setPhotoId(photoId);
    share.setUserId(currentUser.getId());
    share.setCaption(caption);
    share.setCreatedAt(clock.instant());
    shareStore.save(share);
    // Increment share count on photo
    photoStore.incrementShareCount(photoId, 1);
    photo.recordShare();
    log.info("User {} shared photo {} to their profile", currentUser.getId(), photoId);
    return photoConversionService.convertToPhotoResponse(photo, java.util.Optional.of(currentUser));
  }

  public List<ShareResult> getPhotoShares(String photoId) {
    List<Share> shares = shareStore.findByPhotoId(photoId);
    List<String> userIds = shares.stream().map(Share::getUserId).distinct().toList();
    Map<String, User> userMap =
        userStore.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
    return shares.stream()
        .map(
            share -> {
              User user = userMap.get(share.getUserId());
              return new ShareResult(
                  share.getId(),
                  share.getPhotoId(),
                  share.getUserId(),
                  user == null ? null : user.getUsername(),
                  user == null ? null : userAvatarCacheService.getAvatar(user.getId()),
                  share.getCaption(),
                  share.getCreatedAt());
            })
        .toList();
  }

  public long getShareCount(String photoId) {
    return shareStore.countByPhotoId(photoId);
  }

  public boolean hasShared(String photoId) {
    User currentUser = userService.requireCurrentUser();
    return shareStore.exists(photoId, currentUser.getId());
  }

  public PageResult<ShareWithPhotoResult> getSharesByUserId(String userId, int page, int size) {
    List<Share> shares = shareStore.findByUserId(userId, page, size);
    // Get all photo IDs from shares
    List<String> photoIds = shares.stream().map(Share::getPhotoId).distinct().toList();
    // Fetch original photos
    Map<String, Photo> photoMap =
        photoStore.findAllById(photoIds).stream().collect(Collectors.toMap(Photo::getId, p -> p));
    // Get user info for original photos
    List<String> originalUserIds =
        photoMap.values().stream()
            .map(p -> p.getUser() != null ? p.getUser().getUserId() : null)
            .filter(id -> id != null)
            .distinct()
            .toList();
    Map<String, User> userMap =
        userStore.findByIdIn(originalUserIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u));
    // Get current sharer user info
    User sharerUser = userStore.findById(userId).orElse(null);
    List<ShareWithPhotoResult> items =
        shares.stream()
            .map(
                share -> {
                  ShareWithPhotoResult response = new ShareWithPhotoResult();
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
                })
            .toList();
    long total = shareStore.countByUserId(userId);
    return new PageResult<>(items, page, size, total, (int) Math.ceil((double) total / size));
  }

  /** Get shares by multiple user IDs (for newsfeed) */
  public List<Share> getSharesByUserIds(List<String> userIds) {
    return shareStore.findByUserIds(userIds);
  }

  /** Check if a share is liked by user */
  public boolean isLikedByUser(Share share, User user) {
    return false; // Simplified - shares don't have like feature yet
  }

  public ShareService(
      final ShareStore shareStore,
      final PhotoStore photoStore,
      final UserStore userStore,
      final UserProfileService userService,
      final PhotoConversionService photoConversionService,
      final AvatarCache userAvatarCacheService,
      final Clock clock) {
    this.shareStore = shareStore;
    this.photoStore = photoStore;
    this.userStore = userStore;
    this.userService = userService;
    this.photoConversionService = photoConversionService;
    this.userAvatarCacheService = userAvatarCacheService;
    this.clock = clock;
  }
}
