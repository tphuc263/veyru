package com.veyru.application.result.comment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CommentResult {
  private String id;
  private String photoId;
  private String userId;
  private String username;
  private String userImageUrl;
  private String text;
  private Instant createdAt;
  // Nested comments support
  private String parentCommentId;
  private long likeCount = 0;
  private long replyCount = 0;
  private boolean isLikedByCurrentUser = false;
  // Replies (nested comments) - only loaded for top-level comments
  private List<CommentResult> replies = new ArrayList<>();
  // Mentioned users
  private List<MentionedUser> mentionedUsers = new ArrayList<>();

  public static class MentionedUser {
    private String userId;
    private String username;

    public MentionedUser() {}

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
      if (!(o instanceof CommentResult.MentionedUser)) return false;
      final CommentResult.MentionedUser other = (CommentResult.MentionedUser) o;
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
      return other instanceof CommentResult.MentionedUser;
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
      return "CommentResult.MentionedUser(userId="
          + this.getUserId()
          + ", username="
          + this.getUsername()
          + ")";
    }
  }

  public CommentResult() {}

  public String getId() {
    return this.id;
  }

  public String getPhotoId() {
    return this.photoId;
  }

  public String getUserId() {
    return this.userId;
  }

  public String getUsername() {
    return this.username;
  }

  public String getUserImageUrl() {
    return this.userImageUrl;
  }

  public String getText() {
    return this.text;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
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

  public boolean isLikedByCurrentUser() {
    return this.isLikedByCurrentUser;
  }

  public List<CommentResult> getReplies() {
    return this.replies;
  }

  public List<MentionedUser> getMentionedUsers() {
    return this.mentionedUsers;
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

  public void setUsername(final String username) {
    this.username = username;
  }

  public void setUserImageUrl(final String userImageUrl) {
    this.userImageUrl = userImageUrl;
  }

  public void setText(final String text) {
    this.text = text;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
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

  public void setLikedByCurrentUser(final boolean isLikedByCurrentUser) {
    this.isLikedByCurrentUser = isLikedByCurrentUser;
  }

  public void setReplies(final List<CommentResult> replies) {
    this.replies = replies;
  }

  public void setMentionedUsers(final List<MentionedUser> mentionedUsers) {
    this.mentionedUsers = mentionedUsers;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof CommentResult)) return false;
    final CommentResult other = (CommentResult) o;
    if (!other.canEqual((Object) this)) return false;
    if (this.getLikeCount() != other.getLikeCount()) return false;
    if (this.getReplyCount() != other.getReplyCount()) return false;
    if (this.isLikedByCurrentUser() != other.isLikedByCurrentUser()) return false;
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
    final Object this$username = this.getUsername();
    final Object other$username = other.getUsername();
    if (this$username == null ? other$username != null : !this$username.equals(other$username))
      return false;
    final Object this$userImageUrl = this.getUserImageUrl();
    final Object other$userImageUrl = other.getUserImageUrl();
    if (this$userImageUrl == null
        ? other$userImageUrl != null
        : !this$userImageUrl.equals(other$userImageUrl)) return false;
    final Object this$text = this.getText();
    final Object other$text = other.getText();
    if (this$text == null ? other$text != null : !this$text.equals(other$text)) return false;
    final Object this$createdAt = this.getCreatedAt();
    final Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
      return false;
    final Object this$parentCommentId = this.getParentCommentId();
    final Object other$parentCommentId = other.getParentCommentId();
    if (this$parentCommentId == null
        ? other$parentCommentId != null
        : !this$parentCommentId.equals(other$parentCommentId)) return false;
    final Object this$replies = this.getReplies();
    final Object other$replies = other.getReplies();
    if (this$replies == null ? other$replies != null : !this$replies.equals(other$replies))
      return false;
    final Object this$mentionedUsers = this.getMentionedUsers();
    final Object other$mentionedUsers = other.getMentionedUsers();
    if (this$mentionedUsers == null
        ? other$mentionedUsers != null
        : !this$mentionedUsers.equals(other$mentionedUsers)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof CommentResult;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final long $likeCount = this.getLikeCount();
    result = result * PRIME + (int) ($likeCount >>> 32 ^ $likeCount);
    final long $replyCount = this.getReplyCount();
    result = result * PRIME + (int) ($replyCount >>> 32 ^ $replyCount);
    result = result * PRIME + (this.isLikedByCurrentUser() ? 79 : 97);
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $photoId = this.getPhotoId();
    result = result * PRIME + ($photoId == null ? 43 : $photoId.hashCode());
    final Object $userId = this.getUserId();
    result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
    final Object $username = this.getUsername();
    result = result * PRIME + ($username == null ? 43 : $username.hashCode());
    final Object $userImageUrl = this.getUserImageUrl();
    result = result * PRIME + ($userImageUrl == null ? 43 : $userImageUrl.hashCode());
    final Object $text = this.getText();
    result = result * PRIME + ($text == null ? 43 : $text.hashCode());
    final Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    final Object $parentCommentId = this.getParentCommentId();
    result = result * PRIME + ($parentCommentId == null ? 43 : $parentCommentId.hashCode());
    final Object $replies = this.getReplies();
    result = result * PRIME + ($replies == null ? 43 : $replies.hashCode());
    final Object $mentionedUsers = this.getMentionedUsers();
    result = result * PRIME + ($mentionedUsers == null ? 43 : $mentionedUsers.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "CommentResult(id="
        + this.getId()
        + ", photoId="
        + this.getPhotoId()
        + ", userId="
        + this.getUserId()
        + ", username="
        + this.getUsername()
        + ", userImageUrl="
        + this.getUserImageUrl()
        + ", text="
        + this.getText()
        + ", createdAt="
        + this.getCreatedAt()
        + ", parentCommentId="
        + this.getParentCommentId()
        + ", likeCount="
        + this.getLikeCount()
        + ", replyCount="
        + this.getReplyCount()
        + ", isLikedByCurrentUser="
        + this.isLikedByCurrentUser()
        + ", replies="
        + this.getReplies()
        + ", mentionedUsers="
        + this.getMentionedUsers()
        + ")";
  }
}
