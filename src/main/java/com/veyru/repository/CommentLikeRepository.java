package com.veyru.repository;

import com.veyru.model.CommentLike;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentLikeRepository extends MongoRepository<CommentLike, String> {
  boolean existsByCommentIdAndUserId(String commentId, String userId);

  Optional<CommentLike> findByCommentIdAndUserId(String commentId, String userId);

  List<CommentLike> findByCommentIdOrderByCreatedAtDesc(String commentId);

  long countByCommentId(String commentId);

  void deleteAllByCommentId(String commentId);
}
