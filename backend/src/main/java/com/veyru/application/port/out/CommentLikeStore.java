package com.veyru.application.port.out;

import com.veyru.domain.model.CommentLike;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface CommentLikeStore {
  CommentLike save(CommentLike like);

  void delete(CommentLike like);

  Optional<CommentLike> find(String commentId, String userId);

  boolean exists(String commentId, String userId);

  default Set<String> findLikedCommentIds(String userId, Collection<String> commentIds) {
    if (userId == null || commentIds == null || commentIds.isEmpty()) return Set.of();
    return commentIds.stream()
        .filter(commentId -> exists(commentId, userId))
        .collect(Collectors.toSet());
  }

  void deleteAllByCommentId(String commentId);
}
