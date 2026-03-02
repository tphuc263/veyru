package share_app.tphucshareapp.service.photo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.FavoriteRepository;
import share_app.tphucshareapp.repository.LikeRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoConversionService {

    private final ModelMapper modelMapper;
    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;

    public PhotoResponse convertToPhotoResponse(Photo photo, @Nullable User currentUser) {
        PhotoResponse response = modelMapper.map(photo, PhotoResponse.class);
        if (photo.getUser() != null) {
            response.setUsername(photo.getUser().getUsername());
            response.setUserImageUrl(photo.getUser().getUserImageUrl());
        }
        response.setLikeCount((int) photo.getLikeCount());
        response.setCommentCount((int) photo.getCommentCount());
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
