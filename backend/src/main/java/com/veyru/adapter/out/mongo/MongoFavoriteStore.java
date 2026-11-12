package com.veyru.adapter.out.mongo;

import com.veyru.application.port.out.FavoriteStore;
import com.veyru.domain.model.Favorite;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
    return mongo.save(FavoriteDocument.fromDomain(value), COLLECTION).toDomain();
  }

  public void delete(Favorite value) {
    mongo.remove(FavoriteDocument.fromDomain(value), COLLECTION);
  }

  public Optional<Favorite> find(String userId, String photoId) {
    return Optional.ofNullable(
            mongo.findOne(relation(userId, photoId), FavoriteDocument.class, COLLECTION))
        .map(FavoriteDocument::toDomain);
  }

  public boolean exists(String userId, String photoId) {
    return mongo.exists(relation(userId, photoId), FavoriteDocument.class, COLLECTION);
  }

  @Override
  public Set<String> findFavoritedPhotoIds(String userId, Collection<String> photoIds) {
    if (userId == null || photoIds == null || photoIds.isEmpty()) return Set.of();
    Query query = Query.query(Criteria.where("userId").is(userId).and("photoId").in(photoIds));
    query.fields().include("photoId");
    return mongo.find(query, FavoriteDocument.class, COLLECTION).stream()
        .map(FavoriteDocument::photoId)
        .collect(Collectors.toSet());
  }

  public List<Favorite> findByUserId(String userId) {
    return map(
        mongo.find(
            Query.query(Criteria.where("userId").is(userId)), FavoriteDocument.class, COLLECTION));
  }

  public List<Favorite> findByUserId(String userId, int page, int size) {
    Query query = Query.query(Criteria.where("userId").is(userId));
    query.with(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    return map(mongo.find(query, FavoriteDocument.class, COLLECTION));
  }

  public void deleteAllByPhotoId(String photoId) {
    mongo.remove(
        Query.query(Criteria.where("photoId").is(photoId)), FavoriteDocument.class, COLLECTION);
  }

  private List<Favorite> map(List<FavoriteDocument> documents) {
    return documents.stream().map(FavoriteDocument::toDomain).toList();
  }

  private Query relation(String userId, String photoId) {
    return Query.query(Criteria.where("userId").is(userId).and("photoId").is(photoId));
  }

  public MongoFavoriteStore(MongoTemplate mongo) {
    this.mongo = mongo;
  }
}
