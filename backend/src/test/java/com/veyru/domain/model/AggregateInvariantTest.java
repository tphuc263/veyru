package com.veyru.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.veyru.domain.enums.NotificationType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AggregateInvariantTest {
  private static final Instant NOW = Instant.parse("2026-08-23T00:00:00Z");

  @Test
  void photoNormalizesTagsAndInitializesCounters() {
    Photo photo =
        Photo.create(
            "user", "alice", "https://image", " caption ", List.of(" Travel ", "travel"), NOW);

    assertThat(photo.caption()).isEqualTo("caption");
    assertThat(photo.tags()).containsExactly("travel");
    assertThat(photo.likeCount()).isZero();
  }

  @Test
  void followRejectsSelfFollow() {
    assertThatThrownBy(() -> Follow.create("user", "user", NOW))
        .isInstanceOfSatisfying(
            DomainValidationException.class,
            exception -> assertThat(exception.rule()).isEqualTo("follow.self.invalid"));
  }

  @Test
  void commentRejectsBlankTextWrongOwnerAndCrossPhotoReply() {
    assertThatThrownBy(() -> Comment.create("photo", "user", "alice", " ", List.of(), NOW))
        .isInstanceOf(IllegalArgumentException.class);

    Comment comment = Comment.create("photo", "user", "alice", "hello", List.of("mentioned"), NOW);
    assertThatThrownBy(() -> comment.editedBy("other", "edited", List.of()))
        .isInstanceOf(DomainValidationException.class);

    Comment parent = Comment.create("other-photo", "other", "bob", "parent", List.of(), NOW);
    assertThatThrownBy(() -> comment.asReplyTo(parent))
        .isInstanceOf(DomainValidationException.class);
  }

  @Test
  void commentKeepsMentionIdsImmutable() {
    List<String> mentionIds = new ArrayList<>(List.of("mentioned"));
    Comment comment = Comment.create("photo", "user", "alice", "hello", mentionIds, NOW);
    mentionIds.add("other");

    assertThat(comment.mentionedUserIds()).containsExactly("mentioned");
    assertThatThrownBy(() -> comment.mentionedUserIds().add("other"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void notificationMarkReadIsIdempotent() {
    Notification notification =
        Notification.create(
            "recipient",
            "actor",
            "alice",
            NotificationType.NEW_FOLLOWER,
            null,
            null,
            "message",
            null,
            NOW);

    Notification read = notification.markedRead();

    assertThat(read.markedRead()).isSameAs(read);
    assertThat(read.read()).isTrue();
    assertThat(notification.read()).isFalse();
  }

  @Test
  void photoTagRequiresCompleteNormalizedCoordinates() {
    assertThatThrownBy(() -> new PhotoUserTag("tagged", "actor", "alice", .5, null, NOW))
        .isInstanceOfSatisfying(
            DomainValidationException.class,
            exception -> assertThat(exception.rule()).isEqualTo("photo-tag.position.incomplete"));
    assertThatThrownBy(() -> new PhotoUserTag("tagged", "actor", "alice", 1.1, .5, NOW))
        .isInstanceOf(DomainValidationException.class);
  }

  @Test
  void messagingRejectsInvalidParticipantsAndContent() {
    assertThatThrownBy(() -> Conversation.between("user", "user", NOW))
        .isInstanceOfSatisfying(
            DomainValidationException.class,
            exception ->
                assertThat(exception.rule()).isEqualTo("conversation.participants.invalid"));
    assertThatThrownBy(() -> Message.create("conversation", "user", "user", "hello", NOW))
        .isInstanceOfSatisfying(
            DomainValidationException.class,
            exception -> assertThat(exception.rule()).isEqualTo("message.self.invalid"));
    assertThatThrownBy(
            () -> Message.create("conversation", "sender", "receiver", "x".repeat(4_001), NOW))
        .isInstanceOfSatisfying(
            DomainValidationException.class,
            exception -> assertThat(exception.rule()).isEqualTo("message.text.too-long"));
  }

  @Test
  void notificationRequiresReferencesForItsType() {
    assertThatThrownBy(
            () ->
                Notification.create(
                    "recipient",
                    "actor",
                    "alice",
                    NotificationType.LIKE_COMMENT,
                    "photo",
                    null,
                    "message",
                    null,
                    NOW))
        .isInstanceOfSatisfying(
            DomainValidationException.class,
            exception -> assertThat(exception.rule()).isEqualTo("notification.comment.required"));
  }

  @Test
  void relationRecordsUseValueEqualityAndNormalizeCaption() {
    Share first = Share.create("photo", "user", " caption ", NOW);
    Share second = Share.create("photo", "user", "caption", NOW);

    assertThat(first).isEqualTo(second);
    assertThat(first.caption()).isEqualTo("caption");
  }
}
