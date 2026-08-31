package com.veyru.application.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.veyru.application.identity.UserProfileService;
import com.veyru.application.media.PhotoConversionService;
import com.veyru.application.port.out.AffinityCache;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.FavoriteStore;
import com.veyru.application.port.out.FollowStore;
import com.veyru.application.port.out.GraphFeedQuery;
import com.veyru.application.port.out.LikeStore;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.ShareStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.post.UnifiedPostResult.PostType;
import com.veyru.config.AuthProperties;
import com.veyru.config.NewsfeedProperties;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.Share;
import com.veyru.domain.model.User;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NewsfeedServiceTest {
  private static final Instant NOW = Instant.parse("2026-08-25T00:00:00Z");
  private static final AuthProperties AUTH =
      new AuthProperties(
          new AuthProperties.Token(
              "VGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS0zMi1ieXRlcw==", Duration.ofMinutes(15)),
          new AuthProperties.Cookie(false));
  private final FollowStore follows = mock(FollowStore.class);
  private final PhotoStore photos = mock(PhotoStore.class);
  private final ShareStore shares = mock(ShareStore.class);
  private final UserStore users = mock(UserStore.class);
  private final AffinityCache cache = mock(AffinityCache.class);
  private final GraphFeedQuery graph = mock(GraphFeedQuery.class);
  private final UserProfileService userProfiles = mock(UserProfileService.class);
  private final NewsfeedProperties properties =
      new NewsfeedProperties(
          new NewsfeedProperties.Ranking(200, 30, .5, .4, .35, .25, .6, .3, .1, 72, 50),
          new NewsfeedProperties.Cache(Duration.ofMinutes(5), Duration.ofMinutes(5)));
  private final NewsfeedService service =
      new NewsfeedService(
          follows,
          photos,
          shares,
          users,
          cache,
          graph,
          mock(PhotoConversionService.class),
          userProfiles,
          mock(AvatarCache.class),
          mock(LikeStore.class),
          mock(FavoriteStore.class),
          new FeedCursorCodec(AUTH, Clock.fixed(NOW, ZoneOffset.UTC), properties),
          properties,
          new SimpleMeterRegistry(),
          Clock.fixed(NOW, ZoneOffset.UTC));

  @BeforeEach
  void defaults() {
    when(userProfiles.findUserById("viewer")).thenReturn(user("viewer"));
    when(users.findAllById(anyList()))
        .thenAnswer(
            invocation ->
                invocation.<List<String>>getArgument(0).stream().map(this::user).toList());
    when(photos.findByUsersBefore(anyList(), any(), eq(198))).thenReturn(List.of());
    when(shares.findByUsersBefore(anyList(), any(), eq(198))).thenReturn(List.of());
    when(photos.findAllById(anyList())).thenReturn(List.of());
  }

  @Test
  void ranksDeterministicallyAndSeeksWithCursorOnCacheHit() {
    Photo b = photo("b", "author", NOW.minusSeconds(60));
    Photo a = photo("a", "author", NOW.minusSeconds(60));
    when(photos.findByUsersBetween(anyList(), any(), eq(NOW), eq(200))).thenReturn(List.of(b, a));
    when(shares.findByUsersBetween(anyList(), any(), eq(NOW), eq(200))).thenReturn(List.of());
    when(cache.get("viewer")).thenReturn(Optional.of(Map.of("author", .4, "viewer", 1.0)));

    var first = service.getUnifiedNewsfeed("viewer", null, 1);
    var second = service.getUnifiedNewsfeed("viewer", first.nextCursor(), 1);

    assertThat(first.items()).extracting(item -> item.getId()).containsExactly("a");
    assertThat(second.items()).extracting(item -> item.getId()).containsExactly("b");
    verify(graph, never()).getAuthorAffinities(anyString(), anyList());
  }

  @Test
  void ranksShareAtShareTimeUsingOriginalPhotoSignals() {
    Photo original = photo("photo", "original-author", NOW.minus(Duration.ofDays(5)));
    Share share = new Share("share", "photo", "sharer", "Worth reading", NOW.minusSeconds(10));
    when(photos.findByUsersBetween(anyList(), any(), eq(NOW), eq(200))).thenReturn(List.of());
    when(shares.findByUsersBetween(anyList(), any(), eq(NOW), eq(200))).thenReturn(List.of(share));
    when(photos.findAllById(List.of("photo"))).thenReturn(List.of(original));
    when(photos.findByUsersBefore(anyList(), any(), eq(199))).thenReturn(List.of());
    when(shares.findByUsersBefore(anyList(), any(), eq(199))).thenReturn(List.of());
    when(cache.get("viewer")).thenReturn(Optional.of(Map.of("sharer", .4)));

    var result = service.getUnifiedNewsfeed("viewer", null, 20).items().getFirst();

    assertThat(result.getType()).isEqualTo(PostType.SHARE);
    assertThat(result.getId()).isEqualTo("share_share");
    assertThat(result.getCreatedAt()).isEqualTo(share.getCreatedAt());
    assertThat(result.getOriginalPhotoId()).isEqualTo(original.getId());
  }

  @Test
  void loadsAndCachesBatchAffinityOnCacheMiss() {
    when(photos.findByUsersBetween(anyList(), any(), eq(NOW), eq(200)))
        .thenReturn(List.of(photo("photo", "author", NOW.minusSeconds(10))));
    when(shares.findByUsersBetween(anyList(), any(), eq(NOW), eq(200))).thenReturn(List.of());
    when(photos.findByUsersBefore(anyList(), any(), eq(199))).thenReturn(List.of());
    when(shares.findByUsersBefore(anyList(), any(), eq(199))).thenReturn(List.of());
    when(cache.get("viewer")).thenReturn(Optional.empty());
    when(graph.getAuthorAffinities(anyString(), anyList()))
        .thenReturn(List.of(new GraphAffinity("author", true, 0, 0)));

    service.getUnifiedNewsfeed("viewer", null, 20);

    verify(cache)
        .put("viewer", Map.of("viewer", 1.0, "author", .4), properties.cache().affinityTtl());
  }

  @Test
  void fallsBackToPostRankingWhenRedisAndNeo4jFail() {
    when(photos.findByUsersBetween(anyList(), any(), eq(NOW), eq(200)))
        .thenReturn(List.of(photo("photo", "author", NOW.minusSeconds(10))));
    when(shares.findByUsersBetween(anyList(), any(), eq(NOW), eq(200))).thenReturn(List.of());
    when(photos.findByUsersBefore(anyList(), any(), eq(199))).thenReturn(List.of());
    when(shares.findByUsersBefore(anyList(), any(), eq(199))).thenReturn(List.of());
    when(cache.get("viewer")).thenThrow(new IllegalStateException("redis down"));
    when(graph.getAuthorAffinities(anyString(), anyList()))
        .thenThrow(new IllegalStateException("neo4j down"));

    assertThat(service.getUnifiedNewsfeed("viewer", null, 20).items())
        .extracting(item -> item.getId())
        .containsExactly("photo");
  }

  private Photo photo(String id, String authorId, Instant createdAt) {
    return new Photo(
        id,
        "https://example.test/photo.png",
        "caption",
        createdAt,
        List.of("tag"),
        new Photo.EmbeddedUser(authorId, authorId),
        0,
        0,
        0,
        List.of());
  }

  private User user(String id) {
    return new User(
        id, id, id + "@example.test", null, "hash", null, null, null, NOW, 0, 0, 0, null, null);
  }
}
