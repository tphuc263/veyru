package com.veyru.application.social;

import com.veyru.application.identity.UserProfileService;
import com.veyru.application.media.PhotoConversionService;
import com.veyru.application.port.out.FavoriteStore;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.result.photo.PhotoResponse;
import com.veyru.application.error.ApiException;
import com.veyru.application.error.ErrorCode;
import com.veyru.domain.model.Favorite;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.time.Instant;
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

  public void favorite(String photoId) {
    User currentUser = userService.getCurrentUser();
    photoStore.findById(photoId).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    if (favoriteStore.exists(currentUser.getId(), photoId)) return;
    Favorite favorite = new Favorite();
    favorite.setUserId(currentUser.getId());
    favorite.setPhotoId(photoId);
    favorite.setCreatedAt(Instant.now());
    favoriteStore.save(favorite);
  }

  public void unfavorite(String photoId) {
    User currentUser = userService.getCurrentUser();
    favoriteStore.find(currentUser.getId(), photoId).ifPresent(favoriteStore::delete);
  }

  public List<PhotoResponse> getFavorites(int page, int size) {
    User currentUser = userService.getCurrentUser();
    List<Favorite> favorites = favoriteStore.findByUserId(currentUser.getId(), page, size);
    return favorites.stream()
        .map(
            favorite -> {
              Optional<Photo> photoOpt = photoStore.findById(favorite.getPhotoId());
              return photoOpt.map(
                  photo -> photoConversionService.convertToPhotoResponse(photo, currentUser));
            })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  public boolean isFavorited(String photoId) {
    User currentUser = userService.getCurrentUser();
    return favoriteStore.exists(currentUser.getId(), photoId);
  }

  public FavoriteService(
      final FavoriteStore favoriteStore,
      final PhotoStore photoStore,
      final UserProfileService userService,
      final PhotoConversionService photoConversionService) {
    this.favoriteStore = favoriteStore;
    this.photoStore = photoStore;
    this.userService = userService;
    this.photoConversionService = photoConversionService;
  }
}
