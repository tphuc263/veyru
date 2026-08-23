package com.veyru.application.port.out;

import com.veyru.domain.model.Like;
import java.util.List;
import java.util.Optional;

public interface LikeStore {
  Like save(Like like);

  void delete(Like like);

  Optional<Like> find(String photoId, String userId);

  boolean exists(String photoId, String userId);

  List<Like> findByPhotoId(String photoId);

  List<Like> findAll();

  void deleteAllByPhotoId(String photoId);
}
