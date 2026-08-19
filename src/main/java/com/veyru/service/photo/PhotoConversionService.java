package com.veyru.service.photo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import com.veyru.dto.response.photo.PhotoResponse;
import com.veyru.model.Photo;
import com.veyru.model.User;
import com.veyru.repository.FavoriteRepository;
import com.veyru.repository.LikeRepository;
import com.veyru.service.user.UserAvatarCacheService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoConversionService {

    private final ModelMapper modelMapper;
    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final UserAvatarCacheService userAvatarCacheService;

    public PhotoResponse convertToPhotoResponse(Photo photo, @Nullable User currentUser) {
        PhotoResponse response = modelMapper.map(photo, PhotoResponse.class);
        if (photo.getUser() != null) {
            response.setUsername(photo.getUser().getUsername());
            response.setUserImageUrl(userAvatarCacheService.getAvatar(photo.getUser().getUserId()));
        }
        response.setLikeCount((int) photo.getLikeCount());
        response.setCommentCount((int) photo.getCommentCount());
        response.setShareCount((int) photo.getShareCount());
        response.setTags(photo.getTags());

        if (currentUser != null) {
            boolean isLiked = likeRepository.existsByPhotoIdAndUserId(photo.getId(), currentUser.getId());
            response.setLikedByCurrentUser(isLiked);
            boolean isSaved = favoriteRepository.existsByUserIdAndPhotoId(currentUser.getId(), photo.getId());
            response.setSavedByCurrentUser(isSaved);
        } else {
            response.setLikedByCurrentUser(false);
            response.setSavedByCurrentUser(false);
        }

        return response;
    }
}
