package com.veyru.adapter.out.mongo;

import com.veyru.application.port.out.CommentLikeStore;
import com.veyru.domain.model.CommentLike;
import java.util.Optional;
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
