package com.veyru.domain.model;

import com.veyru.domain.enums.NotificationType;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable notification aggregate identified by its persisted ID. Actor and recipient must be
 * distinct, and each notification type enforces its required resource references.
 */
public final class Notification {
  private final String id;
  private final String recipientId;
  private final NotificationType type;
  private final String photoId;
  private final String commentId;
  private final String message;
  private final boolean read;
  private final Instant createdAt;
  private final UserSnapshot actor;
  private final String thumbnailUrl;

  private Notification(
      String id,
      String recipientId,
      NotificationType type,
      String photoId,
      String commentId,
      String message,
      boolean read,
      Instant createdAt,
      UserSnapshot actor,
      String thumbnailUrl) {
    this.id =
        id == null
            ? null
            : DomainRules.required(id, "notification.id.invalid", "Notification ID is invalid");
    this.recipientId =
        DomainRules.required(
            recipientId, "notification.recipient.required", "Notification recipient is required");
    DomainRules.require(
        type != null, "notification.type.required", "Notification type is required");
    this.type = type;
    DomainRules.require(
        actor != null, "notification.actor.required", "Notification actor is required");
    DomainRules.require(
        !recipientId.equals(actor.userId()),
        "notification.self.invalid",
        "Self-notifications are not allowed");
    this.actor = actor;
    this.photoId = normalizeReference(photoId);
    this.commentId = normalizeReference(commentId);
    validateReferences(type, this.photoId, this.commentId);
    this.message =
        DomainRules.required(
            message, "notification.message.required", "Notification message is required");
    this.read = read;
    DomainRules.require(
        createdAt != null, "notification.time.required", "Notification time is required");
    this.createdAt = createdAt;
    this.thumbnailUrl = thumbnailUrl == null ? null : thumbnailUrl.trim();
  }

  public static Notification create(
      String recipientId,
      String actorId,
      String actorUsername,
      NotificationType type,
      String photoId,
      String commentId,
      String message,
      String thumbnailUrl,
      Instant createdAt) {
    return new Notification(
        null,
        recipientId,
        type,
        photoId,
        commentId,
        message,
        false,
        createdAt,
        new UserSnapshot(actorId, actorUsername),
        thumbnailUrl);
  }

  /** Reconstitutes persisted state. New notifications should be created through {@link #create}. */
  public static Notification restore(
      String id,
      String recipientId,
      NotificationType type,
      String photoId,
      String commentId,
      String message,
      boolean read,
      Instant createdAt,
      UserSnapshot actor,
      String thumbnailUrl) {
    return new Notification(
        DomainRules.required(
            id, "notification.id.required", "Persisted notification ID is required"),
        recipientId,
        type,
        photoId,
        commentId,
        message,
        read,
        createdAt,
        actor,
        thumbnailUrl);
  }

  public Notification markedRead() {
    if (read) return this;
    return new Notification(
        id, recipientId, type, photoId, commentId, message, true, createdAt, actor, thumbnailUrl);
  }

  private static String normalizeReference(String value) {
    return value == null ? null : value.trim();
  }

  private static void validateReferences(NotificationType type, String photoId, String commentId) {
    boolean photoRequired = type != NotificationType.NEW_FOLLOWER;
    boolean commentRequired =
        type == NotificationType.COMMENT_PHOTO
            || type == NotificationType.LIKE_COMMENT
            || type == NotificationType.REPLY_COMMENT
            || type == NotificationType.MENTION_IN_COMMENT;
    DomainRules.require(
        !photoRequired || (photoId != null && !photoId.isBlank()),
        "notification.photo.required",
        "Notification type requires a photo reference");
    DomainRules.require(
        !commentRequired || (commentId != null && !commentId.isBlank()),
        "notification.comment.required",
        "Notification type requires a comment reference");
  }

  public String id() {
    return id;
  }

  public String recipientId() {
    return recipientId;
  }

  public String actorId() {
    return actor.userId();
  }

  public NotificationType type() {
    return type;
  }

  public String photoId() {
    return photoId;
  }

  public String commentId() {
    return commentId;
  }

  public String message() {
    return message;
  }

  public boolean read() {
    return read;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public UserSnapshot actor() {
    return actor;
  }

  public String thumbnailUrl() {
    return thumbnailUrl;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    return other instanceof Notification notification && id != null && id.equals(notification.id);
  }

  @Override
  public int hashCode() {
    return id == null ? System.identityHashCode(this) : Objects.hash(id);
  }
}
