package com.veyru.adapter.out.mongo;

import com.veyru.domain.model.Photo;
import com.veyru.domain.model.PhotoUserTag;
import com.veyru.domain.model.UserSnapshot;
import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Mongo representation preserving query paths such as user.userId and userTags.taggedUserId. */
@Document(collection = "photos")
final class PhotoDocument {
  @Id private String id;
  private String imageUrl;
  private String caption;
  private Instant createdAt;
  private List<String> tags;
  private UserSnapshotDocument user;
  private long likeCount;
  private long commentCount;
  private long shareCount;
  private List<PhotoUserTagDocument> userTags;

  static PhotoDocument fromDomain(Photo photo) {
    PhotoDocument document = new PhotoDocument();
    document.id = photo.id();
    document.imageUrl = photo.imageUrl();
    document.caption = photo.caption();
    document.createdAt = photo.createdAt();
    document.tags = photo.tags();
    document.user = UserSnapshotDocument.fromDomain(photo.author());
    document.likeCount = photo.likeCount();
    document.commentCount = photo.commentCount();
    document.shareCount = photo.shareCount();
    document.userTags = photo.userTags().stream().map(PhotoUserTagDocument::fromDomain).toList();
    return document;
  }

  Photo toDomain() {
    return Photo.restore(
        id,
        imageUrl,
        caption,
        createdAt,
        tags,
        user.toDomain(),
        likeCount,
        commentCount,
        shareCount,
        userTags.stream().map(PhotoUserTagDocument::toDomain).toList());
  }

  static final class UserSnapshotDocument {
    private String userId;
    private String username;

    static UserSnapshotDocument fromDomain(UserSnapshot snapshot) {
      UserSnapshotDocument document = new UserSnapshotDocument();
      document.userId = snapshot.userId();
      document.username = snapshot.username();
      return document;
    }

    UserSnapshot toDomain() {
      return new UserSnapshot(userId, username);
    }
  }

  static final class PhotoUserTagDocument {
    private String taggedUserId;
    private String taggedByUserId;
    private String username;
    private Double positionX;
    private Double positionY;
    private Instant createdAt;

    static PhotoUserTagDocument fromDomain(PhotoUserTag tag) {
      PhotoUserTagDocument document = new PhotoUserTagDocument();
      document.taggedUserId = tag.taggedUserId();
      document.taggedByUserId = tag.taggedByUserId();
      document.username = tag.username();
      document.positionX = tag.positionX();
      document.positionY = tag.positionY();
      document.createdAt = tag.createdAt();
      return document;
    }

    PhotoUserTag toDomain() {
      return new PhotoUserTag(
          taggedUserId, taggedByUserId, username, positionX, positionY, createdAt);
    }
  }
}
