package com.veyru.adapter.out.mongo;

import com.veyru.domain.model.Favorite;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "favorites")
final class FavoriteDocument {
  @Id private String id;
  private String userId;
  private String photoId;
  private Instant createdAt;

  static FavoriteDocument fromDomain(Favorite favorite) {
    FavoriteDocument document = new FavoriteDocument();
    document.id = favorite.id();
    document.userId = favorite.userId();
    document.photoId = favorite.photoId();
    document.createdAt = favorite.createdAt();
    return document;
  }

  Favorite toDomain() {
    return new Favorite(id, userId, photoId, createdAt);
  }
}
