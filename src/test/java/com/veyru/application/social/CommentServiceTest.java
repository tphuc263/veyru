package com.veyru.application.social;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.veyru.application.identity.UserProfileService;
import com.veyru.application.notification.NotificationService;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.CommentLikeStore;
import com.veyru.application.port.out.CommentStore;
import com.veyru.application.port.out.GraphProjection;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.common.error.UseCaseError;
import com.veyru.domain.model.Comment;
import com.veyru.domain.model.User;
import java.util.Optional;
import java.time.Clock;
import org.junit.jupiter.api.Test;

class CommentServiceTest {
  private final CommentStore commentStore = mock(CommentStore.class);
  private final UserProfileService userService = mock(UserProfileService.class);
  private final CommentService service =
      new CommentService(
          commentStore,
          mock(CommentLikeStore.class),
          mock(PhotoStore.class),
          mock(UserStore.class),
          userService,
          mock(NotificationService.class),
          mock(AvatarCache.class),
          mock(GraphProjection.class),
          Clock.systemUTC());

  @Test
  void returnsNotFoundWhenUpdatingMissingComment() {
    when(commentStore.findById("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.updateComment("missing", new UpdateCommentCommand("text")))
        .isInstanceOfSatisfying(
            UseCaseException.class,
            exception -> assertThat(exception.code()).isEqualTo(UseCaseError.RESOURCE_NOT_FOUND));
  }

  @Test
  void returnsAccessDeniedWhenUpdatingAnotherUsersComment() {
    Comment comment =
        Comment.create("photo", "owner", "owner", "text", java.util.List.of(), java.time.Instant.EPOCH);
    User actor =
        new User(
            "actor", "actor", "actor@example.com", null, "hash", null, null, null,
            java.time.Instant.EPOCH, 0, 0, 0, null, null);
    when(commentStore.findById("comment")).thenReturn(Optional.of(comment));
    when(userService.requireCurrentUser()).thenReturn(actor);

    assertThatThrownBy(() -> service.updateComment("comment", new UpdateCommentCommand("text")))
        .isInstanceOfSatisfying(
            UseCaseException.class,
            exception -> assertThat(exception.code()).isEqualTo(UseCaseError.ACCESS_DENIED));
  }
}
