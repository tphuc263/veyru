package com.veyru.application.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new photo is created Used to decouple photo creation from newsfeed updates
 */
public class PhotoCreatedEvent extends ApplicationEvent {
  private final String photoId;
  private final String authorId;

  public PhotoCreatedEvent(Object source, String photoId, String authorId) {
    super(source);
    this.photoId = photoId;
    this.authorId = authorId;
  }

  public String getPhotoId() {
    return this.photoId;
  }

  public String getAuthorId() {
    return this.authorId;
  }
}
