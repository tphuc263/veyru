package com.veyru.adapter.in.event;

import com.veyru.application.discovery.NewsfeedService;
import com.veyru.application.event.PhotoCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class PhotoFeedUpdateHandler {
  private final NewsfeedService newsfeed;

  public PhotoFeedUpdateHandler(NewsfeedService newsfeed) {
    this.newsfeed = newsfeed;
  }

  @Async
  @EventListener
  public void handle(PhotoCreatedEvent event) {
    newsfeed.updateFollowersFeeds(event.photoId(), event.authorId());
  }
}
