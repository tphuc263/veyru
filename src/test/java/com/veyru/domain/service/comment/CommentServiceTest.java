package com.veyru.application.social;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.veyru.adapter.in.dto.request.comment.UpdateCommentRequest;
import com.veyru.adapter.out.neo4j.Neo4jGraphAdapter;
import com.veyru.adapter.out.redis.RedisUserAvatarCache;
import com.veyru.application.identity.UserProfileService;
import com.veyru.application.notification.NotificationService;
import com.veyru.application.port.out.CommentLikeStore;
import com.veyru.application.port.out.CommentStore;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.domain.exception.ApiException;
import com.veyru.domain.exception.ErrorCode;
import com.veyru.domain.model.Comment;
import com.veyru.domain.model.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

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
          mock(MongoTemplate.class),
          mock(NotificationService.class),
          mock(RedisUserAvatarCache.class),
          mock(Neo4jGraphAdapter.class));

  @Test
  void returnsNotFoundWhenUpdatingMissingComment() {
    when(commentStore.findById("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.updateComment("missing", new UpdateCommentRequest("text")))
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

    assertThatThrownBy(() -> service.updateComment("comment", new UpdateCommentRequest("text")))
        .isInstanceOfSatisfying(
            ApiException.class,
            exception -> assertThat(exception.code()).isEqualTo(ErrorCode.ACCESS_DENIED));
  }
}
