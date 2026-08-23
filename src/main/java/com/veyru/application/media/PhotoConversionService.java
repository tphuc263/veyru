package com.veyru.application.media;

import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.FavoriteStore;
import com.veyru.application.port.out.LikeStore;
import com.veyru.application.result.photo.PhotoResponse;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhotoConversionService {
  private static final Logger log = LoggerFactory.getLogger(PhotoConversionService.class);
  private final LikeStore likeStore;
  private final FavoriteStore favoriteStore;
  private final AvatarCache userAvatarCacheService;

  public PhotoResponse convertToPhotoResponse(Photo photo, @Nullable User currentUser) {
    PhotoResponse response = new PhotoResponse();
    response.setId(photo.getId());
    response.setImageUrl(photo.getImageUrl());
    response.setCaption(photo.getCaption());
    response.setCreatedAt(photo.getCreatedAt());
    if (photo.getUser() != null) {
      response.setUsername(photo.getUser().getUsername());
      response.setUserId(photo.getUser().getUserId());
      response.setUserImageUrl(userAvatarCacheService.getAvatar(photo.getUser().getUserId()));
    }
    response.setLikeCount((int) photo.getLikeCount());
    response.setCommentCount((int) photo.getCommentCount());
    response.setShareCount((int) photo.getShareCount());
    response.setTags(photo.getTags());
    if (currentUser != null) {
      boolean isLiked = likeStore.exists(photo.getId(), currentUser.getId());
      response.setLikedByCurrentUser(isLiked);
      boolean isSaved = favoriteStore.exists(currentUser.getId(), photo.getId());
      response.setSavedByCurrentUser(isSaved);
    } else {
      response.setLikedByCurrentUser(false);
      response.setSavedByCurrentUser(false);
    }
    return response;
  }

  public PhotoConversionService(
      LikeStore likeStore, FavoriteStore favoriteStore, AvatarCache userAvatarCacheService) {
    this.likeStore = likeStore;
    this.favoriteStore = favoriteStore;
    this.userAvatarCacheService = userAvatarCacheService;
  }
}
