package com.veyru.application.port.out;

import com.veyru.domain.model.Share;
import java.time.Instant;
import java.util.List;

public interface ShareStore {
  Share save(Share share);

  long countByPhotoId(String photoId);

  boolean exists(String photoId, String userId);

  List<Share> findByPhotoId(String photoId);

  List<Share> findByUserId(String userId, int page, int size);

  long countByUserId(String userId);

  List<Share> findByUserIds(List<String> userIds);

  List<Share> findByUsersBetween(List<String> userIds, Instant after, Instant before, int limit);

  List<Share> findByUsersBefore(List<String> userIds, Instant before, int limit);

  void deleteAllByPhotoId(String photoId);
}
