package com.veyru.adapter.out.mongo;

import com.veyru.domain.model.Share;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "shares")
final class ShareDocument {
  @Id private String id;
  private String photoId;
  private String userId;
  private String caption;
  private Instant createdAt;

  static ShareDocument fromDomain(Share share) {
    ShareDocument document = new ShareDocument();
    document.id = share.id();
    document.photoId = share.photoId();
    document.userId = share.userId();
    document.caption = share.caption();
    document.createdAt = share.createdAt();
    return document;
  }

  Share toDomain() {
    return new Share(id, photoId, userId, caption, createdAt);
  }
}
