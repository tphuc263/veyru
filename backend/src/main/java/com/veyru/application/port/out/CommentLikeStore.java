package com.veyru.application.port.out;

import com.veyru.domain.model.CommentLike;
import java.util.Optional;

public interface CommentLikeStore {
  CommentLike save(CommentLike like);

  void delete(CommentLike like);

  Optional<CommentLike> find(String commentId, String userId);

  boolean exists(String commentId, String userId);

  void deleteAllByCommentId(String commentId);
}
