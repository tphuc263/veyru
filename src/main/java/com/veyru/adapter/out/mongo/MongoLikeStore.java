package com.veyru.adapter.out.mongo;

import com.veyru.application.port.out.LikeStore;
import com.veyru.domain.model.Like;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class MongoLikeStore implements LikeStore {
  private static final String COLLECTION = "likes";
  private final MongoTemplate mongo;

  public Like save(Like like) {
    return mongo.save(like, COLLECTION);
  }

  public void delete(Like like) {
    mongo.remove(like, COLLECTION);
  }

  public Optional<Like> find(String photoId, String userId) {
    return Optional.ofNullable(mongo.findOne(relation(photoId, userId), Like.class, COLLECTION));
  }

  public boolean exists(String photoId, String userId) {
    return mongo.exists(relation(photoId, userId), Like.class, COLLECTION);
  }

  public List<Like> findByPhotoId(String photoId) {
    Query query = Query.query(Criteria.where("photoId").is(photoId));
    query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
    return mongo.find(query, Like.class, COLLECTION);
  }

  public List<Like> findAll() {
    return mongo.findAll(Like.class, COLLECTION);
  }

  public void deleteAllByPhotoId(String photoId) {
    mongo.remove(Query.query(Criteria.where("photoId").is(photoId)), Like.class, COLLECTION);
  }

  private Query relation(String photoId, String userId) {
    return Query.query(Criteria.where("photoId").is(photoId).and("userId").is(userId));
  }

  public MongoLikeStore(MongoTemplate mongo) {
    this.mongo = mongo;
  }
}
