package com.veyru.application.port.out;

import com.veyru.domain.model.Comment;
import java.util.List;
import java.util.Optional;

public interface CommentStore {
  Comment save(Comment comment);

  void delete(Comment comment);

  Optional<Comment> findById(String id);

  List<Comment> findTopLevelByPhotoId(String photoId);

  List<Comment> findByPhotoId(String photoId);

  List<Comment> findReplies(String commentId);

  List<Comment> findReplies(String commentId, int page, int size);

  long countByPhotoId(String photoId);

  void incrementLikeCount(String commentId, long delta);

  void incrementReplyCount(String commentId, long delta);

  void deleteAllByPhotoId(String photoId);

  void deleteAllReplies(String commentId);
}
