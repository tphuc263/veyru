package com.veyru.adapter.out.mongo;

import com.veyru.application.port.out.CommentLikeStore;
import com.veyru.domain.model.CommentLike;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class MongoCommentLikeStore implements CommentLikeStore {
  private static final String COLLECTION = "comment_likes";
  private final MongoTemplate mongo;

  public CommentLike save(CommentLike value) {
    return mongo.save(CommentLikeDocument.fromDomain(value), COLLECTION).toDomain();
  }

  public void delete(CommentLike value) {
    mongo.remove(CommentLikeDocument.fromDomain(value), COLLECTION);
  }

  public Optional<CommentLike> find(String commentId, String userId) {
    return Optional.ofNullable(
            mongo.findOne(relation(commentId, userId), CommentLikeDocument.class, COLLECTION))
        .map(CommentLikeDocument::toDomain);
  }

  public boolean exists(String commentId, String userId) {
    return mongo.exists(relation(commentId, userId), CommentLikeDocument.class, COLLECTION);
  }

  @Override
  public Set<String> findLikedCommentIds(String userId, Collection<String> commentIds) {
    if (userId == null || commentIds == null || commentIds.isEmpty()) return Set.of();
    Query query = Query.query(Criteria.where("userId").is(userId).and("commentId").in(commentIds));
    query.fields().include("commentId");
    return mongo.find(query, CommentLikeDocument.class, COLLECTION).stream()
        .map(CommentLikeDocument::commentId)
        .collect(Collectors.toSet());
  }

  public void deleteAllByCommentId(String commentId) {
    mongo.remove(
        Query.query(Criteria.where("commentId").is(commentId)),
        CommentLikeDocument.class,
        COLLECTION);
  }

  private Query relation(String commentId, String userId) {
    return Query.query(Criteria.where("commentId").is(commentId).and("userId").is(userId));
  }

  public MongoCommentLikeStore(MongoTemplate mongo) {
    this.mongo = mongo;
  }
}
