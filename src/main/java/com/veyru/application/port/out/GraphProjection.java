package com.veyru.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface GraphProjection {
  void upsertUser(
      String id, String username, String imageUrl, long followers, long photos, String bio);

  void upsertPhoto(
      String id,
      String userId,
      String username,
      String imageUrl,
      String caption,
      List<String> tags,
      long likes,
      long comments,
      long shares,
      Instant createdAt);

  void createFollowRelationship(String followerId, String followingId);

  void removeFollowRelationship(String followerId, String followingId);

  void createLikeRelationship(String userId, String photoId);

  void removeLikeRelationship(String userId, String photoId);

  void createCommentRelationship(String userId, String photoId);

  Map<String, Long> getGraphStats();
}
