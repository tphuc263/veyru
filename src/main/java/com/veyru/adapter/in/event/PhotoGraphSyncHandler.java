package com.veyru.adapter.in.event;

import com.veyru.application.discovery.GraphSyncService;
import com.veyru.application.event.PhotoCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class PhotoGraphSyncHandler {
  private final GraphSyncService graphSync;

  public PhotoGraphSyncHandler(GraphSyncService graphSync) {
    this.graphSync = graphSync;
  }

  @Async
  @EventListener
  public void handle(PhotoCreatedEvent event) {
    graphSync.syncPhoto(event.photoId());
  }
}
