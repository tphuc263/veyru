package com.veyru.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.veyru.domain.enums.NotificationType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class AggregateInvariantTest {
  private static final Instant NOW = Instant.parse("2026-08-23T00:00:00Z");

  @Test
  void photoNormalizesTagsAndInitializesCounters() {
    Photo photo =
        Photo.create("user", "alice", "https://image", " caption ", List.of(" Travel ", "travel"), NOW);

    assertThat(photo.getCaption()).isEqualTo("caption");
    assertThat(photo.getTags()).containsExactly("travel");
    assertThat(photo.getLikeCount()).isZero();
  }

  @Test
  void followRejectsSelfFollow() {
    assertThatThrownBy(() -> Follow.create("user", "user", NOW))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void commentRejectsBlankTextWrongOwnerAndCrossPhotoReply() {
    assertThatThrownBy(() -> Comment.create("photo", "user", "alice", " ", List.of(), NOW))
        .isInstanceOf(IllegalArgumentException.class);

    Comment comment = Comment.create("photo", "user", "alice", "hello", List.of("mentioned"), NOW);
    assertThatThrownBy(() -> comment.edit("other", "edited", List.of()))
        .isInstanceOf(IllegalArgumentException.class);

    Comment parent = Comment.create("other-photo", "other", "bob", "parent", List.of(), NOW);
    assertThatThrownBy(() -> comment.replyTo(parent)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void notificationMarkReadIsIdempotent() {
    Notification notification =
        Notification.create(
            "recipient", "actor", "alice", NotificationType.NEW_FOLLOWER,
            null, null, "message", null, NOW);

    assertThat(notification.markRead().markRead().isRead()).isTrue();
  }
}
