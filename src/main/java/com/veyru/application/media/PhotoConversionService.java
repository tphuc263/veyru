package com.veyru.application.media;

import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.FavoriteStore;
import com.veyru.application.port.out.LikeStore;
import com.veyru.application.result.photo.PhotoResult;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.util.Optional;

public class PhotoConversionService {
  private final LikeStore likeStore;
  private final FavoriteStore favoriteStore;
  private final AvatarCache userAvatarCacheService;

  public PhotoResult convertToPhotoResponse(Photo photo, Optional<User> currentUser) {
    var owner = photo.getUser();
    User actor = currentUser.orElse(null);
    return new PhotoResult(
        photo.getId(),
        owner == null ? null : owner.getUserId(),
        owner == null ? null : owner.getUsername(),
        owner == null ? null : userAvatarCacheService.getAvatar(owner.getUserId()),
        photo.getImageUrl(),
        photo.getCaption(),
        photo.getCreatedAt(),
        (int) photo.getLikeCount(),
        (int) photo.getCommentCount(),
        (int) photo.getShareCount(),
        actor != null && likeStore.exists(photo.getId(), actor.getId()),
        actor != null && favoriteStore.exists(actor.getId(), photo.getId()),
        photo.getTags());
  }

  public PhotoConversionService(
      LikeStore likeStore, FavoriteStore favoriteStore, AvatarCache userAvatarCacheService) {
    this.likeStore = likeStore;
    this.favoriteStore = favoriteStore;
    this.userAvatarCacheService = userAvatarCacheService;
  }
}
