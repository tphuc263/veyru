package com.veyru.adapter.out.neo4j;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class Neo4jGraphAdapterTest {
  @Container
  static final Neo4jContainer<?> neo4j =
      new Neo4jContainer<>("neo4j:5.26.29").withAdminPassword("test-password");

  static Driver driver;
  static Neo4jGraphAdapter graph;

  @BeforeAll
  static void connect() {
    driver = GraphDatabase.driver(neo4j.getBoltUrl(), AuthTokens.basic("neo4j", "test-password"));
    graph = new Neo4jGraphAdapter(driver);
    for (String id : List.of("viewer", "mutual-a", "mutual-b", "candidate")) {
      graph.upsertUser(id, id, null, 0, 0, null);
    }
    graph.createFollowRelationship("viewer", "mutual-a");
    graph.createFollowRelationship("viewer", "mutual-b");
    graph.createFollowRelationship("mutual-a", "candidate");
    graph.createFollowRelationship("mutual-b", "candidate");
  }

  @AfterAll
  static void disconnect() {
    if (driver != null) driver.close();
  }

  @Test
  void returnsMutualSuggestionsAndDeletesUnfollowedEdges() {
    assertThat(graph.getSuggestedUsers("viewer", 10))
        .singleElement()
        .satisfies(
            suggestion -> {
              assertThat(suggestion.id()).isEqualTo("candidate");
              assertThat(suggestion.score()).isEqualTo(2.0);
            });

    graph.removeFollowRelationship("viewer", "mutual-a");

    assertThat(graph.getAuthorAffinities("viewer", List.of("mutual-a")))
        .singleElement()
        .satisfies(affinity -> assertThat(affinity.followed()).isFalse());
  }

  @Test
  void batchesDirectMutualAndInteractionAffinity() {
    graph.upsertPhoto(
        "candidate-photo",
        "candidate",
        "candidate",
        "https://example.test/photo.png",
        "caption",
        List.of("tag"),
        0,
        0,
        0,
        Instant.EPOCH);
    graph.createLikeRelationship("viewer", "candidate-photo");

    assertThat(graph.getAuthorAffinities("viewer", List.of("candidate")))
        .singleElement()
        .satisfies(
            affinity -> {
              assertThat(affinity.mutualCount()).isPositive();
              assertThat(affinity.interactionCount()).isEqualTo(1);
            });
  }
}
