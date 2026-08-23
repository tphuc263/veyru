package com.veyru.config;

import com.veyru.application.discovery.*;
import com.veyru.application.identity.UserProfileService;
import com.veyru.application.intelligence.AIService;
import com.veyru.application.intelligence.EmbeddingService;
import com.veyru.application.media.PhotoConversionService;
import com.veyru.application.media.PhotoService;
import com.veyru.application.notification.NotificationService;
import com.veyru.application.social.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  ExploreService.class,
  GraphFeedService.class,
  GraphSyncService.class,
  NewsfeedService.class,
  RecommendationService.class,
  SearchService.class,
  UnifiedPostService.class,
  UserProfileService.class,
  AIService.class,
  EmbeddingService.class,
  PhotoConversionService.class,
  PhotoService.class,
  NotificationService.class,
  CommentService.class,
  FavoriteService.class,
  FollowService.class,
  LikeService.class,
  ShareService.class,
  UserTagService.class
})
public class ApplicationServicesConfig {}
