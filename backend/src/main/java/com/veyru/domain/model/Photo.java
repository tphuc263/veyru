package com.veyru.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Immutable photo aggregate identified by its persisted ID. Creation and restoration enforce
 * normalized tags, valid snapshots, and non-negative engagement counters.
 */
public final class Photo {
  private final String id;
  private final String imageUrl;
  private final String caption;
  private final Instant createdAt;
  private final List<String> tags;
  private final UserSnapshot author;
  private final long likeCount;
  private final long commentCount;
  private final long shareCount;
  private final List<PhotoUserTag> userTags;

  private Photo(
      String id,
      String imageUrl,
      String caption,
      Instant createdAt,
      List<String> tags,
      UserSnapshot author,
      long likeCount,
      long commentCount,
      long shareCount,
      List<PhotoUserTag> userTags) {
    this.id =
        id == null ? null : DomainRules.required(id, "photo.id.invalid", "Photo ID is invalid");
    this.imageUrl =
        DomainRules.required(imageUrl, "photo.image.required", "Photo image is required");
    this.caption =
        DomainRules.limited(caption, 2_200, "photo.caption.too-long", "Photo caption is too long");
    DomainRules.require(
        createdAt != null, "photo.time.required", "Photo creation time is required");
    this.createdAt = createdAt;
    this.tags = normalizeTags(tags);
    DomainRules.require(author != null, "photo.author.required", "Photo author is required");
    this.author = author;
    this.likeCount =
        DomainRules.nonNegative(
            likeCount, "photo.like-count.negative", "Like count cannot be negative");
    this.commentCount =
        DomainRules.nonNegative(
            commentCount, "photo.comment-count.negative", "Comment count cannot be negative");
    this.shareCount =
        DomainRules.nonNegative(
            shareCount, "photo.share-count.negative", "Share count cannot be negative");
    this.userTags = userTags == null ? List.of() : List.copyOf(userTags);
  }

  public static Photo create(
      String authorId,
      String authorUsername,
      String imageUrl,
      String caption,
      List<String> tags,
      Instant createdAt) {
    return new Photo(
        null,
        imageUrl,
        caption,
        createdAt,
        tags,
        new UserSnapshot(authorId, authorUsername),
        0,
        0,
        0,
        List.of());
  }

  /** Reconstitutes persisted state. New photos should be created through {@link #create}. */
  public static Photo restore(
      String id,
      String imageUrl,
      String caption,
      Instant createdAt,
      List<String> tags,
      UserSnapshot author,
      long likeCount,
      long commentCount,
      long shareCount,
      List<PhotoUserTag> userTags) {
    return new Photo(
        DomainRules.required(id, "photo.id.required", "Persisted photo ID is required"),
        imageUrl,
        caption,
        createdAt,
        tags,
        author,
        likeCount,
        commentCount,
        shareCount,
        userTags);
  }

  public Photo withRecordedShare() {
    return new Photo(
        id,
        imageUrl,
        caption,
        createdAt,
        tags,
        author,
        likeCount,
        commentCount,
        shareCount + 1,
        userTags);
  }

  private static List<String> normalizeTags(List<String> tags) {
    List<String> normalized =
        tags == null
            ? List.of()
            : tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .map(tag -> tag.toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
    DomainRules.require(
        normalized.size() <= 30 && normalized.stream().allMatch(tag -> tag.length() <= 50),
        "photo.tags.invalid",
        "Photo tags exceed the supported limits");
    return List.copyOf(normalized);
  }

  public String id() {
    return id;
  }

  public String imageUrl() {
    return imageUrl;
  }

  public String caption() {
    return caption;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public List<String> tags() {
    return tags;
  }

  public UserSnapshot author() {
    return author;
  }

  public long likeCount() {
    return likeCount;
  }

  public long commentCount() {
    return commentCount;
  }

  public long shareCount() {
    return shareCount;
  }

  public List<PhotoUserTag> userTags() {
    return userTags;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    return other instanceof Photo photo && id != null && id.equals(photo.id);
  }

  @Override
  public int hashCode() {
    return id == null ? System.identityHashCode(this) : Objects.hash(id);
  }
}
