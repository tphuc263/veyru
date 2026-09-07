package com.veyru.adapter.out.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import org.bson.Document;
import org.junit.jupiter.api.Test;

class MongoCounterUpdateTest {
  @Test
  void guardsDecrementsAgainstCrossingBelowZero() {
    Document query = MongoCounterUpdate.guardedById("photo", "likeCount", -2).getQueryObject();

    assertThat(query.get("_id")).isEqualTo("photo");
    assertThat(query.get("likeCount")).isEqualTo(new Document("$gte", 2L));
  }

  @Test
  void doesNotRestrictIncrements() {
    Document query = MongoCounterUpdate.guardedById("photo", "likeCount", 1).getQueryObject();

    assertThat(query).containsEntry("_id", "photo").doesNotContainKey("likeCount");
  }
}
