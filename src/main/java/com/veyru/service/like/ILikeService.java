package com.veyru.service.like;

import com.veyru.dto.response.like.LikeResponse;
import com.veyru.dto.response.photo.PhotoResponse;
import java.util.List;

public interface ILikeService {
  PhotoResponse toggleLike(String photoId);

  void like(String photoId);

  void unlike(String photoId);

  List<LikeResponse> getPhotoLikes(String photoId);

  long getPhotoLikesCount(String photoId);
}
