package com.veyru.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new photo is created Used to decouple photo creation from newsfeed updates
 */
@Getter
public class PhotoCreatedEvent extends ApplicationEvent {

  private final String photoId;
  private final String authorId;

  public PhotoCreatedEvent(Object source, String photoId, String authorId) {
    super(source);
    this.photoId = photoId;
    this.authorId = authorId;
  }
}
