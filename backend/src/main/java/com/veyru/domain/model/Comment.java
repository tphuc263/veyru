package com.veyru.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Immutable comment aggregate identified by its persisted ID. It owns author-only editing, valid
 * reply relationships, normalized mentions, and non-negative counters.
 */
public final class Comment {
  private final String id;
  private final String photoId;
  private final String text;
  private final Instant createdAt;
  private final UserSnapshot author;
  private final String parentCommentId;
  private final long likeCount;
  private final long replyCount;
  private final List<String> mentionedUserIds;

  private Comment(
      String id,
      String photoId,
      String text,
      Instant createdAt,
      UserSnapshot author,
      String parentCommentId,
      long likeCount,
      long replyCount,
      List<String> mentionedUserIds) {
    this.id =
        id == null ? null : DomainRules.required(id, "comment.id.invalid", "Comment ID is invalid");
    this.photoId =
        DomainRules.required(photoId, "comment.photo.required", "Comment photo is required");
    this.text = validateText(text);
    DomainRules.require(createdAt != null, "comment.time.required", "Comment time is required");
    this.createdAt = createdAt;
    DomainRules.require(author != null, "comment.author.required", "Comment author is required");
    this.author = author;
    this.parentCommentId =
        parentCommentId == null
            ? null
            : DomainRules.required(
                parentCommentId, "comment.parent.invalid", "Parent comment ID is invalid");
    this.likeCount =
        DomainRules.nonNegative(
            likeCount, "comment.like-count.negative", "Like count cannot be negative");
    this.replyCount =
        DomainRules.nonNegative(
            replyCount, "comment.reply-count.negative", "Reply count cannot be negative");
    this.mentionedUserIds = normalizeMentions(mentionedUserIds);
  }

  public static Comment create(
      String photoId,
      String userId,
      String username,
      String text,
      List<String> mentionedUserIds,
      Instant createdAt) {
    return new Comment(
        null,
        photoId,
        text,
        createdAt,
        new UserSnapshot(userId, username),
        null,
        0,
        0,
        mentionedUserIds);
  }

  /** Reconstitutes persisted state. New comments should be created through {@link #create}. */
  public static Comment restore(
      String id,
      String photoId,
      String text,
      Instant createdAt,
      UserSnapshot author,
      String parentCommentId,
      long likeCount,
      long replyCount,
      List<String> mentionedUserIds) {
    return new Comment(
        DomainRules.required(id, "comment.id.required", "Persisted comment ID is required"),
        photoId,
        text,
        createdAt,
        author,
        parentCommentId,
        likeCount,
        replyCount,
        mentionedUserIds);
  }

  public Comment asReplyTo(Comment parent) {
    DomainRules.require(
        parent != null && parent.id != null && photoId.equals(parent.photoId),
        "comment.reply.invalid-parent",
        "Reply parent must be persisted and belong to the same photo");
    return new Comment(
        id, photoId, text, createdAt, author, parent.id, likeCount, replyCount, mentionedUserIds);
  }

  public Comment editedBy(String actorId, String text, List<String> mentionedUserIds) {
    DomainRules.require(
        author.userId().equals(actorId),
        "comment.edit.not-author",
        "Only the comment author can edit it");
    return new Comment(
        id,
        photoId,
        text,
        createdAt,
        author,
        parentCommentId,
        likeCount,
        replyCount,
        mentionedUserIds);
  }

  public Comment withRecordedLike() {
    return new Comment(
        id,
        photoId,
        text,
        createdAt,
        author,
        parentCommentId,
        likeCount + 1,
        replyCount,
        mentionedUserIds);
  }

  private static String validateText(String text) {
    String normalized =
        DomainRules.required(text, "comment.text.required", "Comment text is required");
    DomainRules.require(
        normalized.length() <= 500,
        "comment.text.too-long",
        "Comment text cannot exceed 500 characters");
    return normalized;
  }

  private static List<String> normalizeMentions(List<String> mentionedUserIds) {
    if (mentionedUserIds == null) return List.of();
    return mentionedUserIds.stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(id -> !id.isEmpty())
        .distinct()
        .toList();
  }

  public String id() {
    return id;
  }

  public String photoId() {
    return photoId;
  }

  public String userId() {
    return author.userId();
  }

  public String text() {
    return text;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public UserSnapshot author() {
    return author;
  }

  public String parentCommentId() {
    return parentCommentId;
  }

  public long likeCount() {
    return likeCount;
  }

  public long replyCount() {
    return replyCount;
  }

  public List<String> mentionedUserIds() {
    return mentionedUserIds;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    return other instanceof Comment comment && id != null && id.equals(comment.id);
  }

  @Override
  public int hashCode() {
    return id == null ? System.identityHashCode(this) : Objects.hash(id);
  }
}
