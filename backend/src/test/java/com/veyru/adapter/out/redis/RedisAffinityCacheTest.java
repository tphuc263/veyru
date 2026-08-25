package com.veyru.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.veyru.application.common.PageResult;
import com.veyru.application.discovery.RecommendationService;
import com.veyru.application.intelligence.EmbeddingService;
import com.veyru.application.media.PhotoConversionService;
import com.veyru.application.port.out.CurrentActor;
import com.veyru.application.port.out.FollowStore;
import com.veyru.application.port.out.GraphFeedQuery;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.photo.PhotoResult;
import com.veyru.config.RedisConfig;
import com.veyru.domain.model.Photo;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
class RedisAffinityCacheTest {
  @Container
  static final GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis/redis-stack-server:7.4.0-v8"))
          .withExposedPorts(6379);

  static LettuceConnectionFactory connectionFactory;
  static RedisTemplate<String, Object> template;
  static RedisAffinityCache cache;

  @BeforeAll
  static void connect() {
    connectionFactory = new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
    connectionFactory.afterPropertiesSet();
    template = new RedisConfig().redisTemplate(connectionFactory, new ObjectMapper());
    cache = new RedisAffinityCache(template);
  }

  @AfterAll
  static void disconnect() {
    if (connectionFactory != null) connectionFactory.destroy();
  }

  @Test
  void serializesAffinityAndExpiresIt() throws InterruptedException {
    cache.put("viewer", Map.of("author", .75), Duration.ofMillis(100));

    assertThat(cache.get("viewer")).contains(Map.of("author", .75));
    Thread.sleep(150);
    assertThat(cache.get("viewer")).isEmpty();
  }

  @Test
  void relatedPhotosUseTagsWhenTheSearchIndexIsUnavailable() {
    PhotoStore photos = mock(PhotoStore.class);
    PhotoConversionService conversion = mock(PhotoConversionService.class);
    Photo source = photo("source");
    Photo candidate = photo("candidate");
    PhotoResult expected = mock(PhotoResult.class);
    when(photos.findById("source")).thenReturn(Optional.of(source));
    when(photos.findByTags(any(), any()))
        .thenReturn(new PageResult<>(List.of(source, candidate), 0, 3, 2, 1));
    when(conversion.convertToPhotoResponse(candidate, Optional.empty())).thenReturn(expected);
    RecommendationService recommendations =
        new RecommendationService(
            new EmbeddingService(),
            new RedisVectorAdapter(template),
            mock(GraphFeedQuery.class),
            photos,
            mock(UserStore.class),
            mock(FollowStore.class),
            conversion,
            mock(CurrentActor.class));

    assertThat(recommendations.getRelatedPhotos("source", 2, Optional.empty()))
        .containsExactly(expected);
  }

  private Photo photo(String id) {
    return new Photo(
        id,
        "https://example.test/photo.png",
        "caption",
        Instant.EPOCH,
        List.of("tag"),
        new Photo.EmbeddedUser("author", "author"),
        0,
        0,
        0,
        List.of());
  }
}
