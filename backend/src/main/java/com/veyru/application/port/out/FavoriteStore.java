package com.veyru.application.port.out;

import com.veyru.domain.model.Favorite;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface FavoriteStore {
  Favorite save(Favorite favorite);

  void delete(Favorite favorite);

  Optional<Favorite> find(String userId, String photoId);

  boolean exists(String userId, String photoId);

  default Set<String> findFavoritedPhotoIds(String userId, Collection<String> photoIds) {
    if (userId == null || photoIds == null || photoIds.isEmpty()) return Set.of();
    return photoIds.stream().filter(photoId -> exists(userId, photoId)).collect(Collectors.toSet());
  }

  List<Favorite> findByUserId(String userId);

  List<Favorite> findByUserId(String userId, int page, int size);

  void deleteAllByPhotoId(String photoId);
}
