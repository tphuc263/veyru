package com.veyru.application.port.out;

import com.veyru.domain.model.Favorite;
import java.util.List;
import java.util.Optional;

public interface FavoriteStore {
  Favorite save(Favorite favorite);

  void delete(Favorite favorite);

  Optional<Favorite> find(String userId, String photoId);

  boolean exists(String userId, String photoId);

  List<Favorite> findByUserId(String userId);

  List<Favorite> findByUserId(String userId, int page, int size);

  void deleteAllByPhotoId(String photoId);
}
