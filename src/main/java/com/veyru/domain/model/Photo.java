package com.veyru.domain.model;

import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "photos")
public class Photo {
  @Id private String id;
  private String imageUrl;
  private String caption;
  private Instant createdAt;
  private List<String> tags;
  private EmbeddedUser user;
  private long likeCount;
  private long commentCount;
  private long shareCount;
  private List<EmbeddedUserTag> userTags;

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
      if (!(o instanceof Photo.EmbeddedUser)) return false;
      final Photo.EmbeddedUser other = (Photo.EmbeddedUser) o;
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
      return other instanceof Photo.EmbeddedUser;
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
      return "Photo.EmbeddedUser(userId="
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

  public static class EmbeddedUserTag {
    private String taggedUserId;
    private String taggedByUserId;
    private String username;
    private Double positionX;
    private Double positionY;
    private Instant createdAt;

    public String getTaggedUserId() {
      return this.taggedUserId;
    }

    public String getTaggedByUserId() {
      return this.taggedByUserId;
    }

    public String getUsername() {
      return this.username;
    }

    public Double getPositionX() {
      return this.positionX;
    }

    public Double getPositionY() {
      return this.positionY;
    }

    public Instant getCreatedAt() {
      return this.createdAt;
    }

    public void setTaggedUserId(final String taggedUserId) {
      this.taggedUserId = taggedUserId;
    }

    public void setTaggedByUserId(final String taggedByUserId) {
      this.taggedByUserId = taggedByUserId;
    }

    public void setUsername(final String username) {
      this.username = username;
    }

    public void setPositionX(final Double positionX) {
      this.positionX = positionX;
    }

    public void setPositionY(final Double positionY) {
      this.positionY = positionY;
    }

    public void setCreatedAt(final Instant createdAt) {
      this.createdAt = createdAt;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof Photo.EmbeddedUserTag)) return false;
      final Photo.EmbeddedUserTag other = (Photo.EmbeddedUserTag) o;
      if (!other.canEqual((Object) this)) return false;
      final Object this$positionX = this.getPositionX();
      final Object other$positionX = other.getPositionX();
      if (this$positionX == null
          ? other$positionX != null
          : !this$positionX.equals(other$positionX)) return false;
      final Object this$positionY = this.getPositionY();
      final Object other$positionY = other.getPositionY();
      if (this$positionY == null
          ? other$positionY != null
          : !this$positionY.equals(other$positionY)) return false;
      final Object this$taggedUserId = this.getTaggedUserId();
      final Object other$taggedUserId = other.getTaggedUserId();
      if (this$taggedUserId == null
          ? other$taggedUserId != null
          : !this$taggedUserId.equals(other$taggedUserId)) return false;
      final Object this$taggedByUserId = this.getTaggedByUserId();
      final Object other$taggedByUserId = other.getTaggedByUserId();
      if (this$taggedByUserId == null
          ? other$taggedByUserId != null
          : !this$taggedByUserId.equals(other$taggedByUserId)) return false;
      final Object this$username = this.getUsername();
      final Object other$username = other.getUsername();
      if (this$username == null ? other$username != null : !this$username.equals(other$username))
        return false;
      final Object this$createdAt = this.getCreatedAt();
      final Object other$createdAt = other.getCreatedAt();
      if (this$createdAt == null
          ? other$createdAt != null
          : !this$createdAt.equals(other$createdAt)) return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof Photo.EmbeddedUserTag;
    }

    @Override
    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final Object $positionX = this.getPositionX();
      result = result * PRIME + ($positionX == null ? 43 : $positionX.hashCode());
      final Object $positionY = this.getPositionY();
      result = result * PRIME + ($positionY == null ? 43 : $positionY.hashCode());
      final Object $taggedUserId = this.getTaggedUserId();
      result = result * PRIME + ($taggedUserId == null ? 43 : $taggedUserId.hashCode());
      final Object $taggedByUserId = this.getTaggedByUserId();
      result = result * PRIME + ($taggedByUserId == null ? 43 : $taggedByUserId.hashCode());
      final Object $username = this.getUsername();
      result = result * PRIME + ($username == null ? 43 : $username.hashCode());
      final Object $createdAt = this.getCreatedAt();
      result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
      return result;
    }

    @Override
    public String toString() {
      return "Photo.EmbeddedUserTag(taggedUserId="
          + this.getTaggedUserId()
          + ", taggedByUserId="
          + this.getTaggedByUserId()
          + ", username="
          + this.getUsername()
          + ", positionX="
          + this.getPositionX()
          + ", positionY="
          + this.getPositionY()
          + ", createdAt="
          + this.getCreatedAt()
          + ")";
    }

    public EmbeddedUserTag() {}

    public EmbeddedUserTag(
        final String taggedUserId,
        final String taggedByUserId,
        final String username,
        final Double positionX,
        final Double positionY,
        final Instant createdAt) {
      this.taggedUserId = taggedUserId;
      this.taggedByUserId = taggedByUserId;
      this.username = username;
      this.positionX = positionX;
      this.positionY = positionY;
      this.createdAt = createdAt;
    }
  }

  public String getId() {
    return this.id;
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

  public List<String> getTags() {
    return this.tags;
  }

  public EmbeddedUser getUser() {
    return this.user;
  }

  public long getLikeCount() {
    return this.likeCount;
  }

  public long getCommentCount() {
    return this.commentCount;
  }

  public long getShareCount() {
    return this.shareCount;
  }

  public List<EmbeddedUserTag> getUserTags() {
    return this.userTags;
  }

  public void setId(final String id) {
    this.id = id;
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

  public void setTags(final List<String> tags) {
    this.tags = tags;
  }

  public void setUser(final EmbeddedUser user) {
    this.user = user;
  }

  public void setLikeCount(final long likeCount) {
    this.likeCount = likeCount;
  }

  public void setCommentCount(final long commentCount) {
    this.commentCount = commentCount;
  }

  public void setShareCount(final long shareCount) {
    this.shareCount = shareCount;
  }

  public void setUserTags(final List<EmbeddedUserTag> userTags) {
    this.userTags = userTags;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof Photo)) return false;
    final Photo other = (Photo) o;
    if (!other.canEqual((Object) this)) return false;
    if (this.getLikeCount() != other.getLikeCount()) return false;
    if (this.getCommentCount() != other.getCommentCount()) return false;
    if (this.getShareCount() != other.getShareCount()) return false;
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
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
    final Object this$tags = this.getTags();
    final Object other$tags = other.getTags();
    if (this$tags == null ? other$tags != null : !this$tags.equals(other$tags)) return false;
    final Object this$user = this.getUser();
    final Object other$user = other.getUser();
    if (this$user == null ? other$user != null : !this$user.equals(other$user)) return false;
    final Object this$userTags = this.getUserTags();
    final Object other$userTags = other.getUserTags();
    if (this$userTags == null ? other$userTags != null : !this$userTags.equals(other$userTags))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof Photo;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final long $likeCount = this.getLikeCount();
    result = result * PRIME + (int) ($likeCount >>> 32 ^ $likeCount);
    final long $commentCount = this.getCommentCount();
    result = result * PRIME + (int) ($commentCount >>> 32 ^ $commentCount);
    final long $shareCount = this.getShareCount();
    result = result * PRIME + (int) ($shareCount >>> 32 ^ $shareCount);
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $imageUrl = this.getImageUrl();
    result = result * PRIME + ($imageUrl == null ? 43 : $imageUrl.hashCode());
    final Object $caption = this.getCaption();
    result = result * PRIME + ($caption == null ? 43 : $caption.hashCode());
    final Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    final Object $tags = this.getTags();
    result = result * PRIME + ($tags == null ? 43 : $tags.hashCode());
    final Object $user = this.getUser();
    result = result * PRIME + ($user == null ? 43 : $user.hashCode());
    final Object $userTags = this.getUserTags();
    result = result * PRIME + ($userTags == null ? 43 : $userTags.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "Photo(id="
        + this.getId()
        + ", imageUrl="
        + this.getImageUrl()
        + ", caption="
        + this.getCaption()
        + ", createdAt="
        + this.getCreatedAt()
        + ", tags="
        + this.getTags()
        + ", user="
        + this.getUser()
        + ", likeCount="
        + this.getLikeCount()
        + ", commentCount="
        + this.getCommentCount()
        + ", shareCount="
        + this.getShareCount()
        + ", userTags="
        + this.getUserTags()
        + ")";
  }

  public Photo() {}

  public Photo(
      final String id,
      final String imageUrl,
      final String caption,
      final Instant createdAt,
      final List<String> tags,
      final EmbeddedUser user,
      final long likeCount,
      final long commentCount,
      final long shareCount,
      final List<EmbeddedUserTag> userTags) {
    this.id = id;
    this.imageUrl = imageUrl;
    this.caption = caption;
    this.createdAt = createdAt;
    this.tags = tags;
    this.user = user;
    this.likeCount = likeCount;
    this.commentCount = commentCount;
    this.shareCount = shareCount;
    this.userTags = userTags;
  }
}
