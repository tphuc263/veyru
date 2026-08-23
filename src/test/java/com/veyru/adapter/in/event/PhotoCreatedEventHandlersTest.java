package com.veyru.adapter.in.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.veyru.application.discovery.GraphSyncService;
import com.veyru.application.discovery.NewsfeedService;
import com.veyru.application.discovery.RecommendationService;
import com.veyru.application.event.PhotoCreatedEvent;
import org.junit.jupiter.api.Test;

class PhotoCreatedEventHandlersTest {
  private static final PhotoCreatedEvent EVENT = new PhotoCreatedEvent("photo-1", "author-1");

  @Test
  void updatesFollowerFeeds() {
    NewsfeedService newsfeed = mock(NewsfeedService.class);

    new PhotoFeedUpdateHandler(newsfeed).handle(EVENT);

    verify(newsfeed).updateFollowersFeeds("photo-1", "author-1");
  }

  @Test
  void syncsThePhotoToTheGraph() {
    GraphSyncService graphSync = mock(GraphSyncService.class);

    new PhotoGraphSyncHandler(graphSync).handle(EVENT);

    verify(graphSync).syncPhoto("photo-1");
  }

  @Test
  void indexesThePhotoAndAuthor() {
    RecommendationService recommendations = mock(RecommendationService.class);

    new PhotoRecommendationIndexHandler(recommendations).handle(EVENT);

    verify(recommendations).indexNewPhoto("photo-1");
    verify(recommendations).reindexUser("author-1");
  }
}
