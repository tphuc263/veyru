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
import com.veyru.application.error.ApiException;
import com.veyru.application.error.ErrorCode;
import com.veyru.domain.model.Comment;
import com.veyru.domain.model.User;
import java.util.Optional;
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
          mock(GraphProjection.class));

  @Test
  void returnsNotFoundWhenUpdatingMissingComment() {
    when(commentStore.findById("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.updateComment("missing", new UpdateCommentCommand("text")))
        .isInstanceOfSatisfying(
            ApiException.class,
            exception -> assertThat(exception.code()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
  }

  @Test
  void returnsAccessDeniedWhenUpdatingAnotherUsersComment() {
    Comment comment = new Comment();
    comment.setUserId("owner");
    User actor = new User();
    actor.setId("actor");
    when(commentStore.findById("comment")).thenReturn(Optional.of(comment));
    when(userService.getCurrentUser()).thenReturn(actor);

    assertThatThrownBy(() -> service.updateComment("comment", new UpdateCommentCommand("text")))
        .isInstanceOfSatisfying(
            ApiException.class,
            exception -> assertThat(exception.code()).isEqualTo(ErrorCode.ACCESS_DENIED));
  }
}
