package com.veyru.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "comments")
public class Comment {
  @Id private String id;
  @Indexed private String photoId;
  private String userId;
  private String text;
  private Instant createdAt;
  private EmbeddedUser user;
  // Nested comments support
  @Indexed private String parentCommentId; // null for top-level comments
  private long likeCount = 0;
  private long replyCount = 0;
  // Mentioned users in comment (when user types @username)
  private List<String> mentionedUserIds = new ArrayList<>();

  public static class EmbeddedUser {
    private String userId;
    private String username;

    public String getUserId() {
      return this.userId;
    }

    public String getUsername() {
      return this.username;
    }

    public void setUserId(final String userId) {
      this.userId = userId;
    }

    public void setUsername(final String username) {
      this.username = username;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof Comment.EmbeddedUser)) return false;
      final Comment.EmbeddedUser other = (Comment.EmbeddedUser) o;
      if (!other.canEqual((Object) this)) return false;
      final Object this$userId = this.getUserId();
      final Object other$userId = other.getUserId();
      if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId))
        return false;
      final Object this$username = this.getUsername();
      final Object other$username = other.getUsername();
      if (this$username == null ? other$username != null : !this$username.equals(other$username))
        return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof Comment.EmbeddedUser;
    }

    @Override
    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final Object $userId = this.getUserId();
      result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
      final Object $username = this.getUsername();
      result = result * PRIME + ($username == null ? 43 : $username.hashCode());
      return result;
    }

    @Override
    public String toString() {
      return "Comment.EmbeddedUser(userId="
          + this.getUserId()
          + ", username="
          + this.getUsername()
          + ")";
    }

    public EmbeddedUser() {}

    public EmbeddedUser(final String userId, final String username) {
      this.userId = userId;
      this.username = username;
    }
  }

  public String getId() {
    return this.id;
  }

  public String getPhotoId() {
    return this.photoId;
  }

  public String getUserId() {
    return this.userId;
  }

  public String getText() {
    return this.text;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public EmbeddedUser getUser() {
    return this.user;
  }

  public String getParentCommentId() {
    return this.parentCommentId;
  }

  public long getLikeCount() {
    return this.likeCount;
  }

  public long getReplyCount() {
    return this.replyCount;
  }

  public List<String> getMentionedUserIds() {
    return this.mentionedUserIds;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setPhotoId(final String photoId) {
    this.photoId = photoId;
  }

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public void setText(final String text) {
    this.text = text;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  public void setUser(final EmbeddedUser user) {
    this.user = user;
  }

  public void setParentCommentId(final String parentCommentId) {
    this.parentCommentId = parentCommentId;
  }

  public void setLikeCount(final long likeCount) {
    this.likeCount = likeCount;
  }

  public void setReplyCount(final long replyCount) {
    this.replyCount = replyCount;
  }

  public void setMentionedUserIds(final List<String> mentionedUserIds) {
    this.mentionedUserIds = mentionedUserIds;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof Comment)) return false;
    final Comment other = (Comment) o;
    if (!other.canEqual((Object) this)) return false;
    if (this.getLikeCount() != other.getLikeCount()) return false;
    if (this.getReplyCount() != other.getReplyCount()) return false;
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final Object this$photoId = this.getPhotoId();
    final Object other$photoId = other.getPhotoId();
    if (this$photoId == null ? other$photoId != null : !this$photoId.equals(other$photoId))
      return false;
    final Object this$userId = this.getUserId();
    final Object other$userId = other.getUserId();
    if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId))
      return false;
    final Object this$text = this.getText();
    final Object other$text = other.getText();
    if (this$text == null ? other$text != null : !this$text.equals(other$text)) return false;
    final Object this$createdAt = this.getCreatedAt();
    final Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
      return false;
    final Object this$user = this.getUser();
    final Object other$user = other.getUser();
    if (this$user == null ? other$user != null : !this$user.equals(other$user)) return false;
    final Object this$parentCommentId = this.getParentCommentId();
    final Object other$parentCommentId = other.getParentCommentId();
    if (this$parentCommentId == null
        ? other$parentCommentId != null
        : !this$parentCommentId.equals(other$parentCommentId)) return false;
    final Object this$mentionedUserIds = this.getMentionedUserIds();
    final Object other$mentionedUserIds = other.getMentionedUserIds();
    if (this$mentionedUserIds == null
        ? other$mentionedUserIds != null
        : !this$mentionedUserIds.equals(other$mentionedUserIds)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof Comment;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final long $likeCount = this.getLikeCount();
    result = result * PRIME + (int) ($likeCount >>> 32 ^ $likeCount);
    final long $replyCount = this.getReplyCount();
    result = result * PRIME + (int) ($replyCount >>> 32 ^ $replyCount);
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $photoId = this.getPhotoId();
    result = result * PRIME + ($photoId == null ? 43 : $photoId.hashCode());
    final Object $userId = this.getUserId();
    result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
    final Object $text = this.getText();
    result = result * PRIME + ($text == null ? 43 : $text.hashCode());
    final Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    final Object $user = this.getUser();
    result = result * PRIME + ($user == null ? 43 : $user.hashCode());
    final Object $parentCommentId = this.getParentCommentId();
    result = result * PRIME + ($parentCommentId == null ? 43 : $parentCommentId.hashCode());
    final Object $mentionedUserIds = this.getMentionedUserIds();
    result = result * PRIME + ($mentionedUserIds == null ? 43 : $mentionedUserIds.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "Comment(id="
        + this.getId()
        + ", photoId="
        + this.getPhotoId()
        + ", userId="
        + this.getUserId()
        + ", text="
        + this.getText()
        + ", createdAt="
        + this.getCreatedAt()
        + ", user="
        + this.getUser()
        + ", parentCommentId="
        + this.getParentCommentId()
        + ", likeCount="
        + this.getLikeCount()
        + ", replyCount="
        + this.getReplyCount()
        + ", mentionedUserIds="
        + this.getMentionedUserIds()
        + ")";
  }

  public Comment() {}

  public Comment(
      final String id,
      final String photoId,
      final String userId,
      final String text,
      final Instant createdAt,
      final EmbeddedUser user,
      final String parentCommentId,
      final long likeCount,
      final long replyCount,
      final List<String> mentionedUserIds) {
    this.id = id;
    this.photoId = photoId;
    this.userId = userId;
    this.text = text;
    this.createdAt = createdAt;
    this.user = user;
    this.parentCommentId = parentCommentId;
    this.likeCount = likeCount;
    this.replyCount = replyCount;
    this.mentionedUserIds = mentionedUserIds;
  }
}
