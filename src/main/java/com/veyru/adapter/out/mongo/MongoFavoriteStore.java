package com.veyru.adapter.out.mongo;

import com.veyru.application.port.out.FavoriteStore;
import com.veyru.domain.model.Favorite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class MongoFavoriteStore implements FavoriteStore {
  private static final String COLLECTION = "favorites";
  private final MongoTemplate mongo;

  public Favorite save(Favorite value) {
    return mongo.save(value, COLLECTION);
  }

  public void delete(Favorite value) {
    mongo.remove(value, COLLECTION);
  }

  public Optional<Favorite> find(String userId, String photoId) {
    return Optional.ofNullable(
        mongo.findOne(relation(userId, photoId), Favorite.class, COLLECTION));
  }

  public boolean exists(String userId, String photoId) {
    return mongo.exists(relation(userId, photoId), Favorite.class, COLLECTION);
  }

  public List<Favorite> findByUserId(String userId) {
    return mongo.find(Query.query(Criteria.where("userId").is(userId)), Favorite.class, COLLECTION);
  }

  public List<Favorite> findByUserId(String userId, int page, int size) {
    Query query = Query.query(Criteria.where("userId").is(userId));
    query.with(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    return mongo.find(query, Favorite.class, COLLECTION);
  }

  public void deleteAllByPhotoId(String photoId) {
    mongo.remove(Query.query(Criteria.where("photoId").is(photoId)), Favorite.class, COLLECTION);
  }

  private Query relation(String userId, String photoId) {
    return Query.query(Criteria.where("userId").is(userId).and("photoId").is(photoId));
  }

  public MongoFavoriteStore(MongoTemplate mongo) {
    this.mongo = mongo;
  }
}
