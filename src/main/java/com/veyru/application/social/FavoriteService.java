package com.veyru.application.social;

import com.veyru.application.identity.UserProfileService;
import com.veyru.application.media.PhotoConversionService;
import com.veyru.application.port.out.FavoriteStore;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.result.photo.PhotoResult;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.common.error.UseCaseError;
import com.veyru.domain.model.Favorite;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.time.Instant;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FavoriteService {
  private static final Logger log = LoggerFactory.getLogger(FavoriteService.class);
  private final FavoriteStore favoriteStore;
  private final PhotoStore photoStore;
  private final UserProfileService userService;
  private final PhotoConversionService photoConversionService;
  private final Clock clock;

  public void favorite(String photoId) {
    User currentUser = userService.requireCurrentUser();
    photoStore.findById(photoId).orElseThrow(() -> new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND));
    if (favoriteStore.exists(currentUser.getId(), photoId)) return;
    Favorite favorite = new Favorite();
    favorite.setUserId(currentUser.getId());
    favorite.setPhotoId(photoId);
    favorite.setCreatedAt(clock.instant());
    favoriteStore.save(favorite);
  }

  public void unfavorite(String photoId) {
    User currentUser = userService.requireCurrentUser();
    favoriteStore.find(currentUser.getId(), photoId).ifPresent(favoriteStore::delete);
  }

  public List<PhotoResult> getFavorites(int page, int size) {
    User currentUser = userService.requireCurrentUser();
    List<Favorite> favorites = favoriteStore.findByUserId(currentUser.getId(), page, size);
    return favorites.stream()
        .map(
            favorite -> {
              Optional<Photo> photoOpt = photoStore.findById(favorite.getPhotoId());
              return photoOpt.map(
                  photo -> photoConversionService.convertToPhotoResponse(photo, java.util.Optional.of(currentUser)));
            })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  public boolean isFavorited(String photoId) {
    User currentUser = userService.requireCurrentUser();
    return favoriteStore.exists(currentUser.getId(), photoId);
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
