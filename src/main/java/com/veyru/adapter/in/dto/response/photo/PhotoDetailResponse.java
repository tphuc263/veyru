package com.veyru.adapter.in.dto.response.photo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.veyru.adapter.in.dto.response.comment.CommentResponse;
import com.veyru.adapter.in.dto.response.like.LikeResponse;
import java.time.Instant;
import java.util.List;

public class PhotoDetailResponse {
  private String id;
  private String userId;
  private String username;
  private String userImageUrl;
  private String imageUrl;
  private String caption;
  private Instant createdAt;
  private int likeCount;
  private int commentCount;
  private int shareCount;

  @JsonProperty("isLikedByCurrentUser")
  private boolean isLikedByCurrentUser;

  @JsonProperty("isSavedByCurrentUser")
  private boolean isSavedByCurrentUser;

  private List<LikeResponse> likes;
  private List<CommentResponse> comments;
  private List<String> tags;

  public PhotoDetailResponse() {}

  public String getId() {
    return this.id;
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

  public String getImageUrl() {
    return this.imageUrl;
  }

  public String getCaption() {
    return this.caption;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public int getLikeCount() {
    return this.likeCount;
  }

  public int getCommentCount() {
    return this.commentCount;
  }

  public int getShareCount() {
    return this.shareCount;
  }

  public boolean isLikedByCurrentUser() {
    return this.isLikedByCurrentUser;
  }

  public boolean isSavedByCurrentUser() {
    return this.isSavedByCurrentUser;
  }

  public List<LikeResponse> getLikes() {
    return this.likes;
  }

  public List<CommentResponse> getComments() {
    return this.comments;
  }

  public List<String> getTags() {
    return this.tags;
  }

  public void setId(final String id) {
    this.id = id;
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

  public void setImageUrl(final String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public void setCaption(final String caption) {
    this.caption = caption;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  public void setLikeCount(final int likeCount) {
    this.likeCount = likeCount;
  }

  public void setCommentCount(final int commentCount) {
    this.commentCount = commentCount;
  }

  public void setShareCount(final int shareCount) {
    this.shareCount = shareCount;
  }

  @JsonProperty("isLikedByCurrentUser")
  public void setLikedByCurrentUser(final boolean isLikedByCurrentUser) {
    this.isLikedByCurrentUser = isLikedByCurrentUser;
  }

  @JsonProperty("isSavedByCurrentUser")
  public void setSavedByCurrentUser(final boolean isSavedByCurrentUser) {
    this.isSavedByCurrentUser = isSavedByCurrentUser;
  }

  public void setLikes(final List<LikeResponse> likes) {
    this.likes = likes;
  }

  public void setComments(final List<CommentResponse> comments) {
    this.comments = comments;
  }

  public void setTags(final List<String> tags) {
    this.tags = tags;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof PhotoDetailResponse)) return false;
    final PhotoDetailResponse other = (PhotoDetailResponse) o;
    if (!other.canEqual((Object) this)) return false;
    if (this.getLikeCount() != other.getLikeCount()) return false;
    if (this.getCommentCount() != other.getCommentCount()) return false;
    if (this.getShareCount() != other.getShareCount()) return false;
    if (this.isLikedByCurrentUser() != other.isLikedByCurrentUser()) return false;
    if (this.isSavedByCurrentUser() != other.isSavedByCurrentUser()) return false;
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
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
    final Object this$imageUrl = this.getImageUrl();
    final Object other$imageUrl = other.getImageUrl();
    if (this$imageUrl == null ? other$imageUrl != null : !this$imageUrl.equals(other$imageUrl))
      return false;
    final Object this$caption = this.getCaption();
    final Object other$caption = other.getCaption();
    if (this$caption == null ? other$caption != null : !this$caption.equals(other$caption))
      return false;
    final Object this$createdAt = this.getCreatedAt();
    final Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
      return false;
    final Object this$likes = this.getLikes();
    final Object other$likes = other.getLikes();
    if (this$likes == null ? other$likes != null : !this$likes.equals(other$likes)) return false;
    final Object this$comments = this.getComments();
    final Object other$comments = other.getComments();
    if (this$comments == null ? other$comments != null : !this$comments.equals(other$comments))
      return false;
    final Object this$tags = this.getTags();
    final Object other$tags = other.getTags();
    if (this$tags == null ? other$tags != null : !this$tags.equals(other$tags)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof PhotoDetailResponse;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + this.getLikeCount();
    result = result * PRIME + this.getCommentCount();
    result = result * PRIME + this.getShareCount();
    result = result * PRIME + (this.isLikedByCurrentUser() ? 79 : 97);
    result = result * PRIME + (this.isSavedByCurrentUser() ? 79 : 97);
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $userId = this.getUserId();
    result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
    final Object $username = this.getUsername();
    result = result * PRIME + ($username == null ? 43 : $username.hashCode());
    final Object $userImageUrl = this.getUserImageUrl();
    result = result * PRIME + ($userImageUrl == null ? 43 : $userImageUrl.hashCode());
    final Object $imageUrl = this.getImageUrl();
    result = result * PRIME + ($imageUrl == null ? 43 : $imageUrl.hashCode());
    final Object $caption = this.getCaption();
    result = result * PRIME + ($caption == null ? 43 : $caption.hashCode());
    final Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    final Object $likes = this.getLikes();
    result = result * PRIME + ($likes == null ? 43 : $likes.hashCode());
    final Object $comments = this.getComments();
    result = result * PRIME + ($comments == null ? 43 : $comments.hashCode());
    final Object $tags = this.getTags();
    result = result * PRIME + ($tags == null ? 43 : $tags.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "PhotoDetailResponse(id="
        + this.getId()
        + ", userId="
        + this.getUserId()
        + ", username="
        + this.getUsername()
        + ", userImageUrl="
        + this.getUserImageUrl()
        + ", imageUrl="
        + this.getImageUrl()
        + ", caption="
        + this.getCaption()
        + ", createdAt="
        + this.getCreatedAt()
        + ", likeCount="
        + this.getLikeCount()
        + ", commentCount="
        + this.getCommentCount()
        + ", shareCount="
        + this.getShareCount()
        + ", isLikedByCurrentUser="
        + this.isLikedByCurrentUser()
        + ", isSavedByCurrentUser="
        + this.isSavedByCurrentUser()
        + ", likes="
        + this.getLikes()
        + ", comments="
        + this.getComments()
        + ", tags="
        + this.getTags()
        + ")";
  }
}
