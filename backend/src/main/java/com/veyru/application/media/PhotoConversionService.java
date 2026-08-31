package com.veyru.application.media;

import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.FavoriteStore;
import com.veyru.application.port.out.LikeStore;
import com.veyru.application.result.photo.PhotoResult;
import com.veyru.domain.model.Photo;

public class PhotoConversionService {
  private final LikeStore likeStore;
  private final FavoriteStore favoriteStore;
  private final AvatarCache userAvatarCacheService;

  public PhotoResult convertToPhotoResponse(Photo photo, PhotoViewer viewer) {
    var owner = photo.author();
    String viewerId =
        viewer instanceof PhotoViewer.Authenticated authenticated ? authenticated.userId() : null;
    return new PhotoResult(
        photo.id(),
        owner.userId(),
        owner.username(),
        userAvatarCacheService.getAvatar(owner.userId()),
        photo.imageUrl(),
        photo.caption(),
        photo.createdAt(),
        (int) photo.likeCount(),
        (int) photo.commentCount(),
        (int) photo.shareCount(),
        viewerId != null && likeStore.exists(photo.id(), viewerId),
        viewerId != null && favoriteStore.exists(viewerId, photo.id()),
        photo.tags());
  }

  public PhotoConversionService(
      LikeStore likeStore, FavoriteStore favoriteStore, AvatarCache userAvatarCacheService) {
    this.likeStore = likeStore;
    this.favoriteStore = favoriteStore;
    this.userAvatarCacheService = userAvatarCacheService;
  }
}
