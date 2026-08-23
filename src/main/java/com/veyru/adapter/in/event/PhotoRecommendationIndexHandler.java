package com.veyru.adapter.in.event;

import com.veyru.application.discovery.RecommendationService;
import com.veyru.application.event.PhotoCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class PhotoRecommendationIndexHandler {
  private final RecommendationService recommendations;

  public PhotoRecommendationIndexHandler(RecommendationService recommendations) {
    this.recommendations = recommendations;
  }

  @Async
  @EventListener
  public void handle(PhotoCreatedEvent event) {
    recommendations.indexNewPhoto(event.photoId());
    recommendations.reindexUser(event.authorId());
  }
}
