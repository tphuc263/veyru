package com.veyru.domain.service.comment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.veyru.adapter.in.dto.request.comment.UpdateCommentRequest;
import com.veyru.application.port.out.CommentLikeRepository;
import com.veyru.application.port.out.CommentRepository;
import com.veyru.application.port.out.PhotoRepository;
import com.veyru.application.port.out.UserRepository;
import com.veyru.domain.exception.ApiException;
import com.veyru.domain.exception.ErrorCode;
import com.veyru.domain.model.Comment;
import com.veyru.domain.model.User;
import com.veyru.domain.service.graph.Neo4jGraphService;
import com.veyru.domain.service.notification.NotificationService;
import com.veyru.domain.service.user.UserAvatarCacheService;
import com.veyru.domain.service.user.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

class CommentServiceTest {
  private final CommentRepository commentRepository = mock(CommentRepository.class);
  private final UserService userService = mock(UserService.class);
  private final CommentService service =
      new CommentService(
          commentRepository,
          mock(CommentLikeRepository.class),
          mock(PhotoRepository.class),
          mock(UserRepository.class),
          userService,
          mock(MongoTemplate.class),
          mock(NotificationService.class),
          mock(UserAvatarCacheService.class),
          mock(Neo4jGraphService.class));

  @Test
  void returnsNotFoundWhenUpdatingMissingComment() {
    when(commentRepository.findById("missing")).thenReturn(Optional.empty());

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
    when(commentRepository.findById("comment")).thenReturn(Optional.of(comment));
    when(userService.getCurrentUser()).thenReturn(actor);

    assertThatThrownBy(() -> service.updateComment("comment", new UpdateCommentRequest("text")))
        .isInstanceOfSatisfying(
            ApiException.class,
            exception -> assertThat(exception.code()).isEqualTo(ErrorCode.ACCESS_DENIED));
  }
}
