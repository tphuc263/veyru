package com.veyru.adapter.in.event;

import com.veyru.application.discovery.GraphSyncService;
import com.veyru.application.discovery.NewsfeedService;
import com.veyru.application.discovery.RecommendationService;
import com.veyru.application.event.PhotoCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for photo-related events Handles newsfeed updates and AI embedding indexing
 * asynchronously when photos are created
 */
@Component
public class PhotoEventListener {
  private static final Logger log = LoggerFactory.getLogger(PhotoEventListener.class);
  private final NewsfeedService newsfeedService;
  private final RecommendationService recommendationService;
  private final GraphSyncService graphSyncService;

  /**
   * Handle photo creation event by updating followers' newsfeeds Runs asynchronously to avoid
   * blocking photo creation
   */
  @EventListener
  @Async
  public void handlePhotoCreated(PhotoCreatedEvent event) {
    log.info(
        "Handling photo created event - photoId: {}, authorId: {}",
        event.photoId(),
        event.authorId());

    graphSyncService.syncPhoto(event.photoId());
    newsfeedService.updateFollowersFeeds(event.photoId(), event.authorId());
    log.info("Successfully updated followers\' feeds for photo: {}", event.photoId());

    // Index the new photo embedding for AI recommendations

    recommendationService.indexNewPhoto(event.photoId());
    recommendationService.reindexUser(event.authorId());
  }

  public PhotoEventListener(
      final NewsfeedService newsfeedService,
      final RecommendationService recommendationService,
      final GraphSyncService graphSyncService) {
    this.newsfeedService = newsfeedService;
    this.recommendationService = recommendationService;
    this.graphSyncService = graphSyncService;
  }
}
