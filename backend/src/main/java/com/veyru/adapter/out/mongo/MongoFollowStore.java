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
    return mongo.save(follow, COLLECTION);
  }

  @Override
  public void delete(Follow follow) {
    mongo.remove(follow, COLLECTION);
  }

  @Override
  public Optional<Follow> find(String followerId, String followingId) {
    return Optional.ofNullable(
        mongo.findOne(relation(followerId, followingId), Follow.class, COLLECTION));
  }

  @Override
  public boolean exists(String followerId, String followingId) {
    return mongo.exists(relation(followerId, followingId), Follow.class, COLLECTION);
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
    return mongo.find(
        Query.query(Criteria.where("followerId").is(userId)), Follow.class, COLLECTION);
  }

  @Override
  public List<Follow> findByFollowingId(String userId) {
    return mongo.find(
        Query.query(Criteria.where("followingId").is(userId)), Follow.class, COLLECTION);
  }

  @Override
  public List<Follow> findAll() {
    return mongo.findAll(Follow.class, COLLECTION);
  }

  private Query relation(String followerId, String followingId) {
    return Query.query(
        Criteria.where("followerId").is(followerId).and("followingId").is(followingId));
  }

  private List<Follow> paged(Criteria criteria, int page, int size) {
    Query query = Query.query(criteria);
    query.with(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    return mongo.find(query, Follow.class, COLLECTION);
  }

  public MongoFollowStore(MongoTemplate mongo) {
    this.mongo = mongo;
  }
}
