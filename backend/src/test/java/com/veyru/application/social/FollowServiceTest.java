package com.veyru.application.social;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.veyru.application.notification.NotificationService;
import com.veyru.application.port.out.AffinityCache;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.CurrentActor;
import com.veyru.application.port.out.FollowStore;
import com.veyru.application.port.out.GraphProjection;
import com.veyru.application.port.out.UserStore;
import com.veyru.domain.model.Follow;
import com.veyru.domain.model.User;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FollowServiceTest {
  private final FollowStore followStore = mock(FollowStore.class);
  private final UserStore userStore = mock(UserStore.class);
  private final CurrentActor currentActor = mock(CurrentActor.class);
  private final AffinityCache affinityCache = mock(AffinityCache.class);
  private final GraphProjection graph = mock(GraphProjection.class);
  private final FollowService service =
      new FollowService(
          followStore,
          userStore,
          mock(NotificationService.class),
          mock(AvatarCache.class),
          graph,
          affinityCache,
          currentActor,
          Clock.systemUTC());

  @Test
  void anonymousFollowReadUsesFalsePersonalizedFlagWithoutFollowingQuery() {
    User user = user("target");
    when(userStore.findById("target")).thenReturn(Optional.of(user));
    when(followStore.findFollowers("target", 0, 20)).thenReturn(List.of());
    when(currentActor.id()).thenReturn(Optional.empty());

    assertThat(service.getFollowers("target", 0, 20)).isEmpty();

    verify(followStore, never()).findByFollowerId(org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  void authenticatedFollowReadUsesPersonalizedFlag() {
    User target = user("target");
    when(userStore.findById("target")).thenReturn(Optional.of(target));
    when(followStore.findFollowers("target", 0, 20))
        .thenReturn(List.of(Follow.create("follower", "target", Instant.EPOCH)));
    when(userStore.findAllById(List.of("follower"))).thenReturn(List.of(user("follower")));
    when(currentActor.id()).thenReturn(Optional.of("actor"));
    when(followStore.findByFollowerId("actor"))
        .thenReturn(List.of(Follow.create("actor", "follower", Instant.EPOCH)));

    assertThat(service.getFollowers("target", 0, 20).getFirst().isFollowedByCurrentUser()).isTrue();
  }

  @Test
  void followEvictsOnlyTheViewersAffinity() {
    when(currentActor.id()).thenReturn(Optional.of("viewer"));
    when(userStore.findById("viewer")).thenReturn(Optional.of(user("viewer")));
    User updatedTarget =
        new User(
            "target",
            "target",
            "target@example.com",
            null,
            "hash",
            null,
            null,
            null,
            Instant.EPOCH,
            3,
            8,
            99,
            null,
            null);
    when(userStore.findById("target"))
        .thenReturn(Optional.of(user("target")), Optional.of(updatedTarget));
    when(followStore.find("viewer", "target")).thenReturn(Optional.empty());

    service.follow("target");

    verify(affinityCache).evict("viewer");
    verify(graph).upsertUser("target", "target", null, 8, 3, null);
  }

  private User user(String id) {
    return new User(
        id,
        id,
        id + "@example.com",
        null,
        "hash",
        null,
        null,
        null,
        Instant.EPOCH,
        0,
        0,
        0,
        null,
        null);
  }
}
