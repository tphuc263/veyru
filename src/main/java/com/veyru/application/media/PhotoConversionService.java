package com.veyru.application.media;

import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.FavoriteStore;
import com.veyru.application.port.out.LikeStore;
import com.veyru.application.result.photo.PhotoResponse;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.util.Optional;

public class PhotoConversionService {
  private final LikeStore likeStore;
  private final FavoriteStore favoriteStore;
  private final AvatarCache userAvatarCacheService;

  public PhotoResponse convertToPhotoResponse(Photo photo, Optional<User> currentUser) {
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
    if (currentUser.isPresent()) {
      User actor = currentUser.get();
      boolean isLiked = likeStore.exists(photo.getId(), actor.getId());
      response.setLikedByCurrentUser(isLiked);
      boolean isSaved = favoriteStore.exists(actor.getId(), photo.getId());
      response.setSavedByCurrentUser(isSaved);
    } else {
      response.setLikedByCurrentUser(false);
      response.setSavedByCurrentUser(false);
    }
    return response;
  }

  public PhotoResponse convertToPhotoResponse(Photo photo, User currentUser) {
    return convertToPhotoResponse(photo, Optional.ofNullable(currentUser));
  }

  public PhotoConversionService(
      LikeStore likeStore, FavoriteStore favoriteStore, AvatarCache userAvatarCacheService) {
    this.likeStore = likeStore;
    this.favoriteStore = favoriteStore;
    this.userAvatarCacheService = userAvatarCacheService;
  }
}
