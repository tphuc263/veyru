package com.veyru.application.social;

import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.identity.UserProfileService;
import com.veyru.application.media.PhotoConversionService;
import com.veyru.application.media.PhotoViewer;
import com.veyru.application.port.out.FavoriteStore;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.result.photo.PhotoResult;
import com.veyru.domain.model.Favorite;
import com.veyru.domain.model.User;
import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

public class FavoriteService {
  private final FavoriteStore favoriteStore;
  private final PhotoStore photoStore;
  private final UserProfileService userService;
  private final PhotoConversionService photoConversionService;
  private final Clock clock;

  public void favorite(String photoId) {
    User currentUser = userService.requireCurrentUser();
    photoStore
        .findById(photoId)
        .orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    if (favoriteStore.exists(currentUser.id(), photoId)) return;
    favoriteStore.save(Favorite.create(currentUser.id(), photoId, clock.instant()));
  }

  public void unfavorite(String photoId) {
    User currentUser = userService.requireCurrentUser();
    favoriteStore.find(currentUser.id(), photoId).ifPresent(favoriteStore::delete);
  }

  public List<PhotoResult> getFavorites(int page, int size) {
    User currentUser = userService.requireCurrentUser();
    List<Favorite> favorites = favoriteStore.findByUserId(currentUser.id(), page, size);
    PhotoViewer viewer = PhotoViewer.authenticated(currentUser);
    return favorites.stream()
        .flatMap(favorite -> photoStore.findById(favorite.photoId()).stream())
        .map(photo -> photoConversionService.convertToPhotoResponse(photo, viewer))
        .collect(Collectors.toList());
  }

  public boolean isFavorited(String photoId) {
    User currentUser = userService.requireCurrentUser();
    return favoriteStore.exists(currentUser.id(), photoId);
  }

  public FavoriteService(
      final FavoriteStore favoriteStore,
      final PhotoStore photoStore,
      final UserProfileService userService,
      final PhotoConversionService photoConversionService,
      final Clock clock) {
    this.favoriteStore = favoriteStore;
    this.photoStore = photoStore;
    this.userService = userService;
    this.photoConversionService = photoConversionService;
    this.clock = clock;
  }
}
