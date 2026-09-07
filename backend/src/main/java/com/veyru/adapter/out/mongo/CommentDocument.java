package com.veyru.adapter.out.mongo;

import com.veyru.domain.model.Comment;
import com.veyru.domain.model.UserSnapshot;
import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Mongo representation of a comment aggregate. */
@Document(collection = "comments")
final class CommentDocument {
  @Id private String id;
  private String photoId;
  private String text;
  private Instant createdAt;
  private UserSnapshotDocument user;
  private String parentCommentId;
  private long likeCount;
  private long replyCount;
  private List<String> mentionedUserIds;

  static CommentDocument fromDomain(Comment comment) {
    CommentDocument document = new CommentDocument();
    document.id = comment.id();
    document.photoId = comment.photoId();
    document.text = comment.text();
    document.createdAt = comment.createdAt();
    document.user = UserSnapshotDocument.fromDomain(comment.author());
    document.parentCommentId = comment.parentCommentId();
    document.likeCount = comment.likeCount();
    document.replyCount = comment.replyCount();
    document.mentionedUserIds = comment.mentionedUserIds();
    return document;
  }

  Comment toDomain() {
    return Comment.restore(
        id,
        photoId,
        text,
        createdAt,
        user.toDomain(),
        parentCommentId,
        likeCount,
        replyCount,
        mentionedUserIds);
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
}
