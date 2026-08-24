package com.veyru.adapter.out.mongo;

import com.veyru.application.common.PageQuery;
import com.veyru.application.common.PageResult;
import com.veyru.application.port.out.UserStore;
import com.veyru.domain.model.User;
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
public class MongoUserStore implements UserStore {
  private static final String COLLECTION = "users";
  private final MongoTemplate mongo;

  @Override
  public User save(User user) {
    return mongo.save(user, COLLECTION);
  }

  @Override
  public Optional<User> findById(String id) {
    return Optional.ofNullable(mongo.findById(id, User.class, COLLECTION));
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return one(Criteria.where("email").is(email));
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return one(Criteria.where("username").is(username));
  }

  @Override
  public Optional<User> findByPhoneNumber(String phoneNumber) {
    return one(Criteria.where("phoneNumber").is(phoneNumber));
  }

  @Override
  public Optional<User> findByResetToken(String token) {
    return one(Criteria.where("resetToken").is(token));
  }

  @Override
  public boolean existsByEmail(String email) {
    return exists(Criteria.where("email").is(email));
  }

  @Override
  public boolean existsByUsername(String username) {
    return exists(Criteria.where("username").is(username));
  }

  @Override
  public boolean existsByPhoneNumber(String phoneNumber) {
    return exists(Criteria.where("phoneNumber").is(phoneNumber));
  }

  @Override
  public List<User> findAllById(List<String> ids) {
    return mongo.find(Query.query(Criteria.where("_id").in(ids)), User.class, COLLECTION);
  }

  @Override
  public List<User> findAll() {
    return mongo.findAll(User.class, COLLECTION);
  }

  @Override
  public PageResult<User> findAll(PageQuery page) {
    return page(new Query(), page);
  }

  @Override
  public PageResult<User> searchByName(String term, PageQuery page) {
    Criteria criteria =
        new Criteria()
            .orOperator(
                Criteria.where("username").regex(term, "i"),
                Criteria.where("firstName").regex(term, "i"),
                Criteria.where("lastName").regex(term, "i"));
    return page(Query.query(criteria), page);
  }

  @Override
  public PageResult<User> searchByUsername(String term, PageQuery page) {
    return page(Query.query(Criteria.where("username").regex(term, "i")), page);
  }

  @Override
  public void incrementPhotoCount(String userId, long delta) {
    increment(userId, "photoCount", delta);
  }

  @Override
  public void incrementFollowerCount(String userId, long delta) {
    increment(userId, "followerCount", delta);
  }

  @Override
  public void incrementFollowingCount(String userId, long delta) {
    increment(userId, "followingCount", delta);
  }

  private void increment(String userId, String field, long delta) {
    mongo.updateFirst(
        Query.query(Criteria.where("_id").is(userId)), new Update().inc(field, delta), COLLECTION);
  }

  private Optional<User> one(Criteria criteria) {
    return Optional.ofNullable(mongo.findOne(Query.query(criteria), User.class, COLLECTION));
  }

  private boolean exists(Criteria criteria) {
    return mongo.exists(Query.query(criteria), User.class, COLLECTION);
  }

  private PageResult<User> page(Query query, PageQuery page) {
    long total = mongo.count(Query.of(query).limit(-1).skip(-1), User.class, COLLECTION);
    query.with(PageRequest.of(page.page(), page.size(), Sort.by(Sort.Direction.DESC, "createdAt")));
    List<User> items = mongo.find(query, User.class, COLLECTION);
    int pages = (int) Math.ceil((double) total / page.size());
    return new PageResult<>(items, page.page(), page.size(), total, pages);
  }

  public MongoUserStore(MongoTemplate mongo) {
    this.mongo = mongo;
  }
}
