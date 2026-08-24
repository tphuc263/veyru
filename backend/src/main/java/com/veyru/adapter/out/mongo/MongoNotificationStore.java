package com.veyru.adapter.out.mongo;

import com.veyru.application.port.out.NotificationStore;
import com.veyru.domain.model.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class MongoNotificationStore implements NotificationStore {
  private static final String COLLECTION = "notifications";
  private final MongoTemplate mongo;

  public Notification save(Notification value) {
    return mongo.save(value, COLLECTION);
  }

  public void saveAll(List<Notification> values) {
    values.forEach(this::save);
  }

  public Optional<Notification> findById(String id) {
    return Optional.ofNullable(mongo.findById(id, Notification.class, COLLECTION));
  }

  public List<Notification> findByRecipient(String id, int page, int size) {
    Query query = Query.query(Criteria.where("recipientId").is(id));
    query.with(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    return mongo.find(query, Notification.class, COLLECTION);
  }

  public List<Notification> findUnread(String id) {
    Query query = Query.query(Criteria.where("recipientId").is(id).and("read").is(false));
    query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
    return mongo.find(query, Notification.class, COLLECTION);
  }

  public long countUnread(String id) {
    return mongo.count(
        Query.query(Criteria.where("recipientId").is(id).and("read").is(false)),
        Notification.class,
        COLLECTION);
  }

  public MongoNotificationStore(MongoTemplate mongo) {
    this.mongo = mongo;
  }
}
