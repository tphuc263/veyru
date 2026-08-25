package com.veyru.adapter.out.mongo;

import com.veyru.application.common.PageQuery;
import com.veyru.application.common.PageResult;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.domain.model.Photo;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class MongoPhotoStore implements PhotoStore {
  private static final String COLLECTION = "photos";
  private final MongoTemplate mongo;

  public Photo save(Photo value) {
    return mongo.save(value, COLLECTION);
  }

  public Optional<Photo> findById(String id) {
    return Optional.ofNullable(mongo.findById(id, Photo.class, COLLECTION));
  }

  public List<Photo> findAll() {
    return mongo.findAll(Photo.class, COLLECTION);
  }

  public List<Photo> findAllById(List<String> ids) {
    return mongo.find(Query.query(Criteria.where("_id").in(ids)), Photo.class, COLLECTION);
  }

  public PageResult<Photo> findAll(PageQuery page) {
    return page(new Query(), page);
  }

  public PageResult<Photo> findByUser(String id, PageQuery page) {
    return page(Query.query(Criteria.where("user.userId").is(id)), page);
  }

  public PageResult<Photo> searchText(String text, PageQuery page) {
    return page(Query.query(Criteria.where("caption").regex(text, "i")), page);
  }

  public PageResult<Photo> searchCaption(String text, PageQuery page) {
    return searchText(text, page);
  }

  public PageResult<Photo> findByTags(List<String> tags, PageQuery page) {
    return page(Query.query(Criteria.where("tags").in(tags)), page);
  }

  public List<Photo> findByUsersAfter(List<String> ids, Instant after) {
    return sorted(Query.query(Criteria.where("user.userId").in(ids).and("createdAt").gt(after)));
  }

  public List<Photo> findByUsersBetween(
      List<String> ids, Instant after, Instant before, int limit) {
    Query query =
        Query.query(Criteria.where("user.userId").in(ids).and("createdAt").gte(after).lte(before));
    return sorted(query.limit(limit));
  }

  public List<Photo> findByUsersBefore(List<String> ids, Instant before, int limit) {
    Query query = Query.query(Criteria.where("user.userId").in(ids).and("createdAt").lt(before));
    return sorted(query.limit(limit));
  }

  public List<Photo> findByUsers(List<String> ids) {
    return sorted(Query.query(Criteria.where("user.userId").in(ids)));
  }

  public List<Photo> findByUser(String id) {
    return sorted(Query.query(Criteria.where("user.userId").is(id)));
  }

  public List<Photo> findTaggedUser(String id) {
    return sorted(Query.query(Criteria.where("userTags.taggedUserId").is(id)));
  }

  public PageResult<Photo> explore(List<String> excluded, Instant after, PageQuery page) {
    Criteria criteria = Criteria.where("user.userId").nin(excluded).and("createdAt").gte(after);
    return ranked(criteria, page);
  }

  public PageResult<Photo> popular(PageQuery page) {
    return ranked(null, page);
  }

  public long count() {
    return mongo.count(new Query(), Photo.class, COLLECTION);
  }

  public void deleteById(String id) {
    mongo.remove(Query.query(Criteria.where("_id").is(id)), Photo.class, COLLECTION);
  }

  public void incrementLikeCount(String id, long delta) {
    increment(id, "likeCount", delta);
  }

  public void incrementCommentCount(String id, long delta) {
    increment(id, "commentCount", delta);
  }

  public void incrementShareCount(String id, long delta) {
    increment(id, "shareCount", delta);
  }

  public void addUserTag(String id, Photo.EmbeddedUserTag tag) {
    mongo.updateFirst(byId(id), new Update().push("userTags", tag), COLLECTION);
  }

  public void removeUserTag(String id, String userId) {
    mongo.updateFirst(
        byId(id),
        new Update().pull("userTags", Query.query(Criteria.where("taggedUserId").is(userId))),
        COLLECTION);
  }

  private void increment(String id, String field, long delta) {
    mongo.updateFirst(byId(id), new Update().inc(field, delta), COLLECTION);
  }

  private Query byId(String id) {
    return Query.query(Criteria.where("_id").is(id));
  }

  private List<Photo> sorted(Query query) {
    query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
    return mongo.find(query, Photo.class, COLLECTION);
  }

  private PageResult<Photo> page(Query query, PageQuery page) {
    long total = mongo.count(Query.of(query).limit(-1).skip(-1), Photo.class, COLLECTION);
    query.with(PageRequest.of(page.page(), page.size(), Sort.by(Sort.Direction.DESC, "createdAt")));
    return new PageResult<>(
        mongo.find(query, Photo.class, COLLECTION),
        page.page(),
        page.size(),
        total,
        (int) Math.ceil((double) total / page.size()));
  }

  private PageResult<Photo> ranked(Criteria criteria, PageQuery page) {
    var operations =
        new java.util.ArrayList<
            org.springframework.data.mongodb.core.aggregation.AggregationOperation>();
    if (criteria != null) operations.add(Aggregation.match(criteria));
    operations.add(
        Aggregation.addFields()
            .addFieldWithValue(
                "engagementScore",
                new org.bson.Document(
                    "$add",
                    List.of(
                        new org.bson.Document("$multiply", List.of("$likeCount", 2)),
                        new org.bson.Document("$multiply", List.of("$commentCount", 3)))))
            .build());
    operations.add(Aggregation.sort(Sort.by(Sort.Direction.DESC, "engagementScore", "createdAt")));
    operations.add(Aggregation.skip((long) page.page() * page.size()));
    operations.add(Aggregation.limit(page.size()));
    List<Photo> items =
        mongo
            .aggregate(Aggregation.newAggregation(operations), COLLECTION, Photo.class)
            .getMappedResults();
    Query countQuery = criteria == null ? new Query() : Query.query(criteria);
    long total = mongo.count(countQuery, Photo.class, COLLECTION);
    return new PageResult<>(
        items, page.page(), page.size(), total, (int) Math.ceil((double) total / page.size()));
  }

  public MongoPhotoStore(MongoTemplate mongo) {
    this.mongo = mongo;
  }
}
