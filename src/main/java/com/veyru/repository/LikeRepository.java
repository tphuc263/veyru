package com.veyru.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.veyru.model.Like;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends MongoRepository<Like, String> {
    List<Like> findByPhotoIdOrderByCreatedAtDesc(String photoId);

    Optional<Like> findByPhotoIdAndUserId(String photoId, String userId);

    boolean existsByPhotoIdAndUserId(String photoId, String userId);

    void deleteAllByPhotoId(String photoId);
}
