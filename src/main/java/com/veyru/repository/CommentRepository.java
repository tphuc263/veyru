package com.veyru.repository;

import com.veyru.model.Comment;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepository extends MongoRepository<Comment, String> {
  // Get top-level comments (no parent)
  List<Comment> findByPhotoIdAndParentCommentIdIsNullOrderByCreatedAtAsc(String photoId);

  // Get replies to a comment
  List<Comment> findByParentCommentIdOrderByCreatedAtAsc(String parentCommentId);

  // Paginated replies
  Page<Comment> findByParentCommentIdOrderByCreatedAtAsc(String parentCommentId, Pageable pageable);

  // Old methods for backward compatibility
  List<Comment> findByPhotoIdOrderByCreatedAtAsc(String photoId);

  long countByPhotoId(String photoId);

  long countByParentCommentId(String parentCommentId);

  void deleteAllByPhotoId(String photoId);

  void deleteAllByParentCommentId(String parentCommentId);
}
