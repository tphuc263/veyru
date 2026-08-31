package com.veyru.adapter.out.mongo;

import com.veyru.application.port.out.FollowStore;
import com.veyru.domain.model.Follow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class MongoFollowStore implements FollowStore {
  private static final String COLLECTION = "follows";
  private final MongoTemplate mongo;

  @Override
  public Follow save(Follow follow) {
    return mongo.save(FollowDocument.fromDomain(follow), COLLECTION).toDomain();
  }

  @Override
  public void delete(Follow follow) {
    mongo.remove(FollowDocument.fromDomain(follow), COLLECTION);
  }

  @Override
  public Optional<Follow> find(String followerId, String followingId) {
    return Optional.ofNullable(
            mongo.findOne(relation(followerId, followingId), FollowDocument.class, COLLECTION))
        .map(FollowDocument::toDomain);
  }

  @Override
  public boolean exists(String followerId, String followingId) {
    return mongo.exists(relation(followerId, followingId), FollowDocument.class, COLLECTION);
  }

  @Override
  public List<Follow> findFollowers(String userId, int page, int size) {
    return paged(Criteria.where("followingId").is(userId), page, size);
  }

  @Override
  public List<Follow> findFollowing(String userId, int page, int size) {
    return paged(Criteria.where("followerId").is(userId), page, size);
  }

  @Override
  public List<Follow> findByFollowerId(String userId) {
    return map(
        mongo.find(
            Query.query(Criteria.where("followerId").is(userId)),
            FollowDocument.class,
            COLLECTION));
  }

  @Override
  public List<Follow> findByFollowingId(String userId) {
    return map(
        mongo.find(
            Query.query(Criteria.where("followingId").is(userId)),
            FollowDocument.class,
            COLLECTION));
  }

  @Override
  public List<Follow> findAll() {
    return map(mongo.findAll(FollowDocument.class, COLLECTION));
  }

  private Query relation(String followerId, String followingId) {
    return Query.query(
        Criteria.where("followerId").is(followerId).and("followingId").is(followingId));
  }

  private List<Follow> paged(Criteria criteria, int page, int size) {
    Query query = Query.query(criteria);
    query.with(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    return map(mongo.find(query, FollowDocument.class, COLLECTION));
  }

  private List<Follow> map(List<FollowDocument> documents) {
    return documents.stream().map(FollowDocument::toDomain).toList();
  }

  public MongoFollowStore(MongoTemplate mongo) {
    this.mongo = mongo;
  }
}
