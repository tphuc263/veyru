package share_app.tphucshareapp.service.like;

import share_app.tphucshareapp.dto.response.like.LikeResponse;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;

import java.util.List;

public interface ILikeService {
    PhotoResponse toggleLike(String photoId);

    void like(String photoId);

    void unlike(String photoId);

    List<LikeResponse> getPhotoLikes(String photoId);

    long getPhotoLikesCount(String photoId);
}
