package com.veyru.application.port.out;

import com.veyru.domain.model.Share;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ShareRepository extends MongoRepository<Share, String> {

  long countByPhotoId(String photoId);

  boolean existsByPhotoIdAndUserId(String photoId, String userId);

  Page<Share> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

  List<Share> findByPhotoIdOrderByCreatedAtDesc(String photoId);

  List<Share> findByUserIdInOrderByCreatedAtDesc(List<String> userIds);

  void deleteAllByPhotoId(String photoId);
}
