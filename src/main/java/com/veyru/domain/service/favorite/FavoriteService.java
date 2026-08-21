package com.veyru.domain.service.favorite;

import com.veyru.adapter.in.dto.response.photo.PhotoResponse;
import com.veyru.domain.model.Favorite;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import com.veyru.domain.exception.ApiException;
import com.veyru.domain.exception.ErrorCode;
import com.veyru.application.port.out.FavoriteRepository;
import com.veyru.application.port.out.PhotoRepository;
import com.veyru.domain.service.photo.PhotoConversionService;
import com.veyru.domain.service.user.UserService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FavoriteService {
  private static final Logger log = LoggerFactory.getLogger(FavoriteService.class);
  private final FavoriteRepository favoriteRepository;
  private final PhotoRepository photoRepository;
  private final UserService userService;
  private final PhotoConversionService photoConversionService;

  public PhotoResponse toggleFavorite(String photoId) {
    User currentUser = userService.getCurrentUser();
    Photo photo =
        photoRepository
            .findById(photoId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    Optional<Favorite> existingFavorite =
        favoriteRepository.findByUserIdAndPhotoId(currentUser.getId(), photoId);
    if (existingFavorite.isPresent()) {
      // Unsave
      favoriteRepository.delete(existingFavorite.get());
      log.info("User {} unsaved photo {}", currentUser.getId(), photoId);
    } else {
      // Save
      Favorite favorite = new Favorite();
      favorite.setUserId(currentUser.getId());
      favorite.setPhotoId(photoId);
      favorite.setCreatedAt(Instant.now());
      favoriteRepository.save(favorite);
      log.info("User {} saved photo {}", currentUser.getId(), photoId);
    }
    return photoConversionService.convertToPhotoResponse(photo, currentUser);
  }

  public void favorite(String photoId) {
    User currentUser = userService.getCurrentUser();
    photoRepository.findById(photoId).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    if (favoriteRepository.existsByUserIdAndPhotoId(currentUser.getId(), photoId)) return;
    Favorite favorite = new Favorite();
    favorite.setUserId(currentUser.getId());
    favorite.setPhotoId(photoId);
    favorite.setCreatedAt(Instant.now());
    favoriteRepository.save(favorite);
  }

  public void unfavorite(String photoId) {
    User currentUser = userService.getCurrentUser();
    favoriteRepository.findByUserIdAndPhotoId(currentUser.getId(), photoId).ifPresent(favoriteRepository::delete);
  }

  public List<PhotoResponse> getFavorites(int page, int size) {
    User currentUser = userService.getCurrentUser();
    Pageable pageable = PageRequest.of(page, size);
    Page<Favorite> favorites =
        favoriteRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable);
    return favorites.getContent().stream()
        .map(
            favorite -> {
              Optional<Photo> photoOpt = photoRepository.findById(favorite.getPhotoId());
              return photoOpt.map(
                  photo -> photoConversionService.convertToPhotoResponse(photo, currentUser));
            })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  public boolean isFavorited(String photoId) {
    User currentUser = userService.getCurrentUser();
    return favoriteRepository.existsByUserIdAndPhotoId(currentUser.getId(), photoId);
  }

  public FavoriteService(
      final FavoriteRepository favoriteRepository,
      final PhotoRepository photoRepository,
      final UserService userService,
      final PhotoConversionService photoConversionService) {
    this.favoriteRepository = favoriteRepository;
    this.photoRepository = photoRepository;
    this.userService = userService;
    this.photoConversionService = photoConversionService;
  }
}
