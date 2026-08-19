package com.veyru.dto.response.comment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CommentResponse {
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
  private List<CommentResponse> replies = new ArrayList<>();
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

    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
      if (o == this) return true;
      if (!(o instanceof CommentResponse.MentionedUser)) return false;
      final CommentResponse.MentionedUser other = (CommentResponse.MentionedUser) o;
      if (!other.canEqual((java.lang.Object) this)) return false;
      final java.lang.Object this$userId = this.getUserId();
      final java.lang.Object other$userId = other.getUserId();
      if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId))
        return false;
      final java.lang.Object this$username = this.getUsername();
      final java.lang.Object other$username = other.getUsername();
      if (this$username == null ? other$username != null : !this$username.equals(other$username))
        return false;
      return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
      return other instanceof CommentResponse.MentionedUser;
    }

    @java.lang.Override
    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final java.lang.Object $userId = this.getUserId();
      result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
      final java.lang.Object $username = this.getUsername();
      result = result * PRIME + ($username == null ? 43 : $username.hashCode());
      return result;
    }

    @java.lang.Override
    public java.lang.String toString() {
      return "CommentResponse.MentionedUser(userId="
          + this.getUserId()
          + ", username="
          + this.getUsername()
          + ")";
    }
  }

  public CommentResponse() {}

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

  public List<CommentResponse> getReplies() {
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

  public void setReplies(final List<CommentResponse> replies) {
    this.replies = replies;
  }

  public void setMentionedUsers(final List<MentionedUser> mentionedUsers) {
    this.mentionedUsers = mentionedUsers;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof CommentResponse)) return false;
    final CommentResponse other = (CommentResponse) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    if (this.getLikeCount() != other.getLikeCount()) return false;
    if (this.getReplyCount() != other.getReplyCount()) return false;
    if (this.isLikedByCurrentUser() != other.isLikedByCurrentUser()) return false;
    final java.lang.Object this$id = this.getId();
    final java.lang.Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final java.lang.Object this$photoId = this.getPhotoId();
    final java.lang.Object other$photoId = other.getPhotoId();
    if (this$photoId == null ? other$photoId != null : !this$photoId.equals(other$photoId))
      return false;
    final java.lang.Object this$userId = this.getUserId();
    final java.lang.Object other$userId = other.getUserId();
    if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId))
      return false;
    final java.lang.Object this$username = this.getUsername();
    final java.lang.Object other$username = other.getUsername();
    if (this$username == null ? other$username != null : !this$username.equals(other$username))
      return false;
    final java.lang.Object this$userImageUrl = this.getUserImageUrl();
    final java.lang.Object other$userImageUrl = other.getUserImageUrl();
    if (this$userImageUrl == null
        ? other$userImageUrl != null
        : !this$userImageUrl.equals(other$userImageUrl)) return false;
    final java.lang.Object this$text = this.getText();
    final java.lang.Object other$text = other.getText();
    if (this$text == null ? other$text != null : !this$text.equals(other$text)) return false;
    final java.lang.Object this$createdAt = this.getCreatedAt();
    final java.lang.Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
      return false;
    final java.lang.Object this$parentCommentId = this.getParentCommentId();
    final java.lang.Object other$parentCommentId = other.getParentCommentId();
    if (this$parentCommentId == null
        ? other$parentCommentId != null
        : !this$parentCommentId.equals(other$parentCommentId)) return false;
    final java.lang.Object this$replies = this.getReplies();
    final java.lang.Object other$replies = other.getReplies();
    if (this$replies == null ? other$replies != null : !this$replies.equals(other$replies))
      return false;
    final java.lang.Object this$mentionedUsers = this.getMentionedUsers();
    final java.lang.Object other$mentionedUsers = other.getMentionedUsers();
    if (this$mentionedUsers == null
        ? other$mentionedUsers != null
        : !this$mentionedUsers.equals(other$mentionedUsers)) return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof CommentResponse;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final long $likeCount = this.getLikeCount();
    result = result * PRIME + (int) ($likeCount >>> 32 ^ $likeCount);
    final long $replyCount = this.getReplyCount();
    result = result * PRIME + (int) ($replyCount >>> 32 ^ $replyCount);
    result = result * PRIME + (this.isLikedByCurrentUser() ? 79 : 97);
    final java.lang.Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final java.lang.Object $photoId = this.getPhotoId();
    result = result * PRIME + ($photoId == null ? 43 : $photoId.hashCode());
    final java.lang.Object $userId = this.getUserId();
    result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
    final java.lang.Object $username = this.getUsername();
    result = result * PRIME + ($username == null ? 43 : $username.hashCode());
    final java.lang.Object $userImageUrl = this.getUserImageUrl();
    result = result * PRIME + ($userImageUrl == null ? 43 : $userImageUrl.hashCode());
    final java.lang.Object $text = this.getText();
    result = result * PRIME + ($text == null ? 43 : $text.hashCode());
    final java.lang.Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    final java.lang.Object $parentCommentId = this.getParentCommentId();
    result = result * PRIME + ($parentCommentId == null ? 43 : $parentCommentId.hashCode());
    final java.lang.Object $replies = this.getReplies();
    result = result * PRIME + ($replies == null ? 43 : $replies.hashCode());
    final java.lang.Object $mentionedUsers = this.getMentionedUsers();
    result = result * PRIME + ($mentionedUsers == null ? 43 : $mentionedUsers.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "CommentResponse(id="
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
