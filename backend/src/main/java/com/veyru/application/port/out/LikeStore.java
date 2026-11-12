package com.veyru.application.port.out;

import com.veyru.domain.model.Like;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface LikeStore {
  Like save(Like like);

  void delete(Like like);

  Optional<Like> find(String photoId, String userId);

  boolean exists(String photoId, String userId);

  default Set<String> findLikedPhotoIds(String userId, Collection<String> photoIds) {
    if (userId == null || photoIds == null || photoIds.isEmpty()) return Set.of();
    return photoIds.stream().filter(photoId -> exists(photoId, userId)).collect(Collectors.toSet());
  }

  List<Like> findByPhotoId(String photoId);

  List<Like> findAll();

  void deleteAllByPhotoId(String photoId);
}
