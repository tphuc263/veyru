package com.veyru.adapter.out.mongo;

import com.veyru.application.port.out.ShareStore;
import com.veyru.domain.model.Share;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class MongoShareStore implements ShareStore {
  private static final String COLLECTION = "shares";
  private final MongoTemplate mongo;

  public Share save(Share value) {
    return mongo.save(value, COLLECTION);
  }

  public long countByPhotoId(String photoId) {
    return mongo.count(by("photoId", photoId), Share.class, COLLECTION);
  }

  public boolean exists(String photoId, String userId) {
    return mongo.exists(
        Query.query(Criteria.where("photoId").is(photoId).and("userId").is(userId)),
        Share.class,
        COLLECTION);
  }

  public List<Share> findByPhotoId(String photoId) {
    return sorted(by("photoId", photoId));
  }

  public List<Share> findByUserId(String userId, int page, int size) {
    Query query = by("userId", userId);
    query.with(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    return mongo.find(query, Share.class, COLLECTION);
  }

  public long countByUserId(String userId) {
    return mongo.count(by("userId", userId), Share.class, COLLECTION);
  }

  public List<Share> findByUserIds(List<String> ids) {
    return sorted(Query.query(Criteria.where("userId").in(ids)));
  }

  public void deleteAllByPhotoId(String photoId) {
    mongo.remove(by("photoId", photoId), Share.class, COLLECTION);
  }

  private Query by(String field, String value) {
    return Query.query(Criteria.where(field).is(value));
  }

  private List<Share> sorted(Query query) {
    query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
    return mongo.find(query, Share.class, COLLECTION);
  }

  public MongoShareStore(MongoTemplate mongo) {
    this.mongo = mongo;
  }
}
