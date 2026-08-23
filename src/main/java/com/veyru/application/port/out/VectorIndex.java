package com.veyru.application.port.out;

import java.util.List;
import java.util.Map;

public interface VectorIndex {
  void storePhotoEmbedding(
      String photoId, float[] embedding, String caption, String userId, List<String> tags);

  void storeUserEmbedding(String userId, float[] embedding, String username, String bio);

  List<Map<String, Object>> searchSimilarPhotos(float[] embedding, int limit, String excludedId);

  List<Map<String, Object>> searchSimilarUsers(float[] embedding, int limit, String excludedId);

  void deletePhotoEmbedding(String photoId);

  void deleteUserEmbedding(String userId);

  boolean hasPhotoEmbedding(String photoId);

  boolean hasUserEmbedding(String userId);
}
