package com.veyru.application.media;

import com.veyru.application.common.PageResult;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.FavoriteStore;
import com.veyru.application.port.out.LikeStore;
import com.veyru.application.result.photo.PhotoResult;
import com.veyru.domain.model.Photo;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  public List<PhotoResult> convertToPhotoResponses(List<Photo> photos, PhotoViewer viewer) {
    if (photos == null || photos.isEmpty()) return List.of();
    String viewerId =
        viewer instanceof PhotoViewer.Authenticated authenticated ? authenticated.userId() : null;
    List<String> photoIds = photos.stream().map(Photo::id).distinct().toList();
    List<String> authorIds =
        photos.stream().map(photo -> photo.author().userId()).distinct().toList();

    Map<String, String> avatars = userAvatarCacheService.getAvatars(authorIds);
    Set<String> likedIds =
        viewerId == null ? Set.of() : likeStore.findLikedPhotoIds(viewerId, photoIds);
    Set<String> savedIds =
        viewerId == null ? Set.of() : favoriteStore.findFavoritedPhotoIds(viewerId, photoIds);

    return photos.stream()
        .map(
            photo -> {
              var owner = photo.author();
              return new PhotoResult(
                  photo.id(),
                  owner.userId(),
                  owner.username(),
                  avatars.get(owner.userId()),
                  photo.imageUrl(),
                  photo.caption(),
                  photo.createdAt(),
                  (int) photo.likeCount(),
                  (int) photo.commentCount(),
                  (int) photo.shareCount(),
                  likedIds.contains(photo.id()),
                  savedIds.contains(photo.id()),
                  photo.tags());
            })
        .toList();
  }

  public PageResult<PhotoResult> convertToPhotoPage(PageResult<Photo> page, PhotoViewer viewer) {
    List<PhotoResult> items = convertToPhotoResponses(page.items(), viewer);
    return new PageResult<>(
        items, page.page(), page.size(), page.totalElements(), page.totalPages());
  }

  public PhotoConversionService(
      LikeStore likeStore, FavoriteStore favoriteStore, AvatarCache userAvatarCacheService) {
    this.likeStore = likeStore;
    this.favoriteStore = favoriteStore;
    this.userAvatarCacheService = userAvatarCacheService;
  }
}
