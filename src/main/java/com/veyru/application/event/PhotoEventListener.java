package com.veyru.application.event;

import com.veyru.domain.service.ai.RecommendationService;
import com.veyru.domain.service.photo.NewsfeedService;
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

  /**
   * Handle photo creation event by updating followers' newsfeeds Runs asynchronously to avoid
   * blocking photo creation
   */
  @EventListener
  @Async
  public void handlePhotoCreated(PhotoCreatedEvent event) {
    log.info(
        "Handling photo created event - photoId: {}, authorId: {}",
        event.getPhotoId(),
        event.getAuthorId());
    try {
      newsfeedService.updateFollowersFeeds(event.getPhotoId(), event.getAuthorId());
      log.info("Successfully updated followers\' feeds for photo: {}", event.getPhotoId());
    } catch (Exception e) {
      log.error("Error updating followers\' feeds for photo: {}", event.getPhotoId(), e);
    }
    // Index the new photo embedding for AI recommendations
    try {
      recommendationService.indexNewPhoto(event.getPhotoId());
      recommendationService.reindexUser(event.getAuthorId());
    } catch (Exception e) {
      log.warn("Failed to index embedding for photo: {}", event.getPhotoId(), e);
    }
  }

  public PhotoEventListener(
      final NewsfeedService newsfeedService, final RecommendationService recommendationService) {
    this.newsfeedService = newsfeedService;
    this.recommendationService = recommendationService;
  }
}
