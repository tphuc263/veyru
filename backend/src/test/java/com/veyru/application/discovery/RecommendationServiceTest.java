package com.veyru.application.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.veyru.application.common.PageResult;
import com.veyru.application.intelligence.EmbeddingService;
import com.veyru.application.media.PhotoConversionService;
import com.veyru.application.port.out.CurrentActor;
import com.veyru.application.port.out.FollowStore;
import com.veyru.application.port.out.GraphFeedQuery;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.port.out.VectorIndex;
import com.veyru.application.result.photo.PhotoResult;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RecommendationServiceTest {
  private final EmbeddingService embeddings = mock(EmbeddingService.class);
  private final VectorIndex vectors = mock(VectorIndex.class);
  private final GraphFeedQuery graph = mock(GraphFeedQuery.class);
  private final PhotoStore photos = mock(PhotoStore.class);
  private final UserStore users = mock(UserStore.class);
  private final FollowStore follows = mock(FollowStore.class);
  private final PhotoConversionService conversion = mock(PhotoConversionService.class);
  private final CurrentActor actor = mock(CurrentActor.class);
  private final RecommendationService service =
      new RecommendationService(
          embeddings, vectors, graph, photos, users, follows, conversion, actor);

  @Test
  void usesTagMatchingWhenRedisSearchFails() {
    Photo source = photo("source", "viewer");
    Photo candidate = photo("candidate", "author");
    PhotoResult expected = mock(PhotoResult.class);
    when(photos.findById("source")).thenReturn(Optional.of(source));
    when(vectors.hasPhotoEmbedding("source")).thenThrow(new IllegalStateException("redis down"));
    when(photos.findByTags(any(), any()))
        .thenReturn(new PageResult<>(List.of(source, candidate), 0, 3, 2, 1));
    when(conversion.convertToPhotoResponse(candidate, Optional.empty())).thenReturn(expected);

    assertThat(service.getRelatedPhotos("source", 2, Optional.empty())).containsExactly(expected);
  }

  @Test
  void hydratesMutualSuggestionsInOneMongoBatch() {
    User viewer = user("viewer", 0);
    User candidate = user("candidate", 10);
    when(users.findById("viewer")).thenReturn(Optional.of(viewer));
    when(graph.getSuggestedUsers("viewer", 4))
        .thenReturn(List.of(new GraphFeedItem("candidate", 2)));
    when(users.findAllById(List.of("candidate"))).thenReturn(List.of(candidate));

    var result = service.getSuggestedUsers("viewer", 2).getFirst();

    assertThat(result.id()).isEqualTo("candidate");
    assertThat(result.reason()).isEqualTo("Followed by 2 people you follow");
    verify(users).findAllById(List.of("candidate"));
  }

  private Photo photo(String id, String author) {
    return new Photo(
        id,
        "https://example.test/photo.png",
        "caption",
        Instant.EPOCH,
        List.of("tag"),
        new Photo.EmbeddedUser(author, author),
        0,
        0,
        0,
        List.of());
  }

  private User user(String id, long followers) {
    return new User(
        id,
        id,
        id + "@example.test",
        null,
        "hash",
        null,
        null,
        null,
        Instant.EPOCH,
        0,
        followers,
        0,
        null,
        null);
  }
}
