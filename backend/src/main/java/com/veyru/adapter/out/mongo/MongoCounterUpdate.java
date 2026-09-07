package com.veyru.adapter.out.mongo;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

final class MongoCounterUpdate {
  private MongoCounterUpdate() {}

  static Query guardedById(String id, String field, long delta) {
    Criteria criteria = Criteria.where("_id").is(id);
    // A conditional decrement keeps concurrent updates from crossing below zero.
    if (delta < 0) criteria = criteria.and(field).gte(-delta);
    return Query.query(criteria);
  }
}
