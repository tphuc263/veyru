package com.veyru.domain.service.photo;

import com.veyru.adapter.in.dto.response.photo.PhotoResponse;
import com.veyru.application.port.out.FavoriteRepository;
import com.veyru.application.port.out.LikeRepository;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import com.veyru.domain.service.user.UserAvatarCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class PhotoConversionService {
  private static final Logger log = LoggerFactory.getLogger(PhotoConversionService.class);
  private final LikeRepository likeRepository;
  private final FavoriteRepository favoriteRepository;
  private final UserAvatarCacheService userAvatarCacheService;

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
      boolean isLiked = likeRepository.existsByPhotoIdAndUserId(photo.getId(), currentUser.getId());
      response.setLikedByCurrentUser(isLiked);
      boolean isSaved =
          favoriteRepository.existsByUserIdAndPhotoId(currentUser.getId(), photo.getId());
      response.setSavedByCurrentUser(isSaved);
    } else {
      response.setLikedByCurrentUser(false);
      response.setSavedByCurrentUser(false);
    }
    return response;
  }

  public PhotoConversionService(
      LikeRepository likeRepository,
      FavoriteRepository favoriteRepository,
      UserAvatarCacheService userAvatarCacheService) {
    this.likeRepository = likeRepository;
    this.favoriteRepository = favoriteRepository;
    this.userAvatarCacheService = userAvatarCacheService;
  }
}
