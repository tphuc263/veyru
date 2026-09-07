package com.veyru.application.social;

import com.veyru.application.common.PageResult;
import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.identity.UserProfileService;
import com.veyru.application.media.PhotoConversionService;
import com.veyru.application.media.PhotoViewer;
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

public class ShareService {
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
    shareStore.save(Share.create(photoId, currentUser.id(), caption, clock.instant()));
    photoStore.incrementShareCount(photoId, 1);
    Photo updatedPhoto = photo.withRecordedShare();
    return photoConversionService.convertToPhotoResponse(
        updatedPhoto, PhotoViewer.authenticated(currentUser));
  }

  public List<ShareResult> getPhotoShares(String photoId) {
    List<Share> shares = shareStore.findByPhotoId(photoId);
    List<String> userIds = shares.stream().map(Share::userId).distinct().toList();
    Map<String, User> userMap =
        userStore.findAllById(userIds).stream().collect(Collectors.toMap(User::id, u -> u));
    return shares.stream()
        .map(
            share -> {
              User user = userMap.get(share.userId());
              return new ShareResult(
                  share.id(),
                  share.photoId(),
                  share.userId(),
                  user == null ? null : user.username(),
                  user == null ? null : userAvatarCacheService.getAvatar(user.id()),
                  share.caption(),
                  share.createdAt());
            })
        .toList();
  }

  public long getShareCount(String photoId) {
    return shareStore.countByPhotoId(photoId);
  }

  public boolean hasShared(String photoId) {
    User currentUser = userService.requireCurrentUser();
    return shareStore.exists(photoId, currentUser.id());
  }

  public PageResult<ShareWithPhotoResult> getSharesByUserId(String userId, int page, int size) {
    List<Share> shares = shareStore.findByUserId(userId, page, size);
    List<String> photoIds = shares.stream().map(Share::photoId).distinct().toList();
    Map<String, Photo> photoMap =
        photoStore.findAllById(photoIds).stream().collect(Collectors.toMap(Photo::id, p -> p));
    List<String> originalUserIds =
        photoMap.values().stream().map(photo -> photo.author().userId()).distinct().toList();
    Map<String, User> userMap =
        userStore.findAllById(originalUserIds).stream().collect(Collectors.toMap(User::id, u -> u));
    User sharerUser = userStore.findById(userId).orElse(null);
    List<ShareWithPhotoResult> items =
        shares.stream()
            .map(
                share -> {
                  ShareWithPhotoResult response = new ShareWithPhotoResult();
                  response.setId(share.id());
                  response.setPhotoId(share.photoId());
                  response.setUserId(share.userId());
                  response.setCaption(share.caption());
                  response.setCreatedAt(share.createdAt());
                  if (sharerUser != null) {
                    response.setUsername(sharerUser.username());
                    response.setUserImageUrl(userAvatarCacheService.getAvatar(sharerUser.id()));
                  }
                  Photo originalPhoto = photoMap.get(share.photoId());
                  if (originalPhoto != null) {
                    response.setOriginalPhotoId(originalPhoto.id());
                    response.setOriginalImageUrl(originalPhoto.imageUrl());
                    response.setOriginalCaption(originalPhoto.caption());
                    response.setOriginalCreatedAt(originalPhoto.createdAt());
                    response.setOriginalLikeCount((int) originalPhoto.likeCount());
                    response.setOriginalCommentCount((int) originalPhoto.commentCount());
                    response.setOriginalShareCount((int) originalPhoto.shareCount());
                    response.setOriginalUsername(originalPhoto.author().username());
                    User originalUser = userMap.get(originalPhoto.author().userId());
                    if (originalUser != null) {
                      response.setOriginalUserImageUrl(
                          userAvatarCacheService.getAvatar(originalUser.id()));
                    }
                  }
                  return response;
                })
            .toList();
    long total = shareStore.countByUserId(userId);
    return new PageResult<>(items, page, size, total, (int) Math.ceil((double) total / size));
  }

  public List<Share> getSharesByUserIds(List<String> userIds) {
    return shareStore.findByUserIds(userIds);
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
