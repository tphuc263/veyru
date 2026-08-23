package com.veyru.adapter.out.mongo;

import com.veyru.application.port.out.CommentStore;
import com.veyru.domain.model.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class MongoCommentStore implements CommentStore {
  private static final String COLLECTION = "comments";
  private final MongoTemplate mongo;

  public Comment save(Comment value) {
    return mongo.save(value, COLLECTION);
  }

  public void delete(Comment value) {
    mongo.remove(value, COLLECTION);
  }

  public Optional<Comment> findById(String id) {
    return Optional.ofNullable(mongo.findById(id, Comment.class, COLLECTION));
  }

  public List<Comment> findTopLevelByPhotoId(String photoId) {
    return sorted(
        Query.query(Criteria.where("photoId").is(photoId).and("parentCommentId").is(null)));
  }

  public List<Comment> findByPhotoId(String photoId) {
    return sorted(Query.query(Criteria.where("photoId").is(photoId)));
  }

  public List<Comment> findReplies(String id) {
    return sorted(Query.query(Criteria.where("parentCommentId").is(id)));
  }

  public List<Comment> findReplies(String id, int page, int size) {
    Query query = Query.query(Criteria.where("parentCommentId").is(id));
    query.with(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt")));
    return mongo.find(query, Comment.class, COLLECTION);
  }

  public long countByPhotoId(String id) {
    return mongo.count(Query.query(Criteria.where("photoId").is(id)), Comment.class, COLLECTION);
  }

  public void incrementLikeCount(String id, long delta) {
    increment(id, "likeCount", delta);
  }

  public void incrementReplyCount(String id, long delta) {
    increment(id, "replyCount", delta);
  }

  public void deleteAllByPhotoId(String id) {
    mongo.remove(Query.query(Criteria.where("photoId").is(id)), Comment.class, COLLECTION);
  }

  public void deleteAllReplies(String id) {
    mongo.remove(Query.query(Criteria.where("parentCommentId").is(id)), Comment.class, COLLECTION);
  }

  private void increment(String id, String field, long delta) {
    mongo.updateFirst(
        Query.query(Criteria.where("_id").is(id)), new Update().inc(field, delta), COLLECTION);
  }

  private List<Comment> sorted(Query query) {
    query.with(Sort.by(Sort.Direction.ASC, "createdAt"));
    return mongo.find(query, Comment.class, COLLECTION);
  }

  public MongoCommentStore(MongoTemplate mongo) {
    this.mongo = mongo;
  }
}
