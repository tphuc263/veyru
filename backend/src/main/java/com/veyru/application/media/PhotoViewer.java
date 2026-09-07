package com.veyru.application.media;

import com.veyru.domain.model.User;

/** Explicit viewer context used when personalizing photo projections. */
public sealed interface PhotoViewer permits PhotoViewer.Anonymous, PhotoViewer.Authenticated {
  static PhotoViewer anonymous() {
    return Anonymous.INSTANCE;
  }

  static PhotoViewer authenticated(User user) {
    return new Authenticated(user.id());
  }

  enum Anonymous implements PhotoViewer {
    INSTANCE
  }

  record Authenticated(String userId) implements PhotoViewer {
    public Authenticated {
      if (userId == null || userId.isBlank()) {
        throw new IllegalArgumentException("Authenticated viewer ID is required");
      }
      userId = userId.trim();
    }
  }
}
