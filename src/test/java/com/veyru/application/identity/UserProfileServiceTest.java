package com.veyru.application.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.CurrentActor;
import com.veyru.application.port.out.ImageStorage;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.social.FollowService;
import com.veyru.domain.model.User;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class UserProfileServiceTest {
  private final UserStore users = mock(UserStore.class);
  private final FollowService follows = mock(FollowService.class);
  private final CurrentActor currentActor = mock(CurrentActor.class);
  private final UserProfileService service =
      new UserProfileService(
          users, mock(ImageStorage.class), follows, mock(AvatarCache.class), currentActor);

  @Test
  void anonymousProfileReadUsesFalseFollowingFlagWithoutFollowingQuery() {
    when(users.findById("target")).thenReturn(Optional.of(user("target")));
    when(currentActor.id()).thenReturn(Optional.empty());

    assertThat(service.getUserProfileById("target").isFollowingByCurrentUser()).isFalse();

    verifyNoInteractions(follows);
  }

  @Test
  void authenticatedProfileReadUsesFollowingFlag() {
    User actor = user("actor");
    when(users.findById("target")).thenReturn(Optional.of(user("target")));
    when(currentActor.id()).thenReturn(Optional.of("actor"));
    when(users.findById("actor")).thenReturn(Optional.of(actor));
    when(follows.isFollowing("actor", "target")).thenReturn(true);

    assertThat(service.getUserProfileById("target").isFollowingByCurrentUser()).isTrue();
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
