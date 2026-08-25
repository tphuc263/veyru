package com.veyru.application.port.out;

import java.util.List;
import java.util.Map;

public interface VectorIndex {
  void storePhotoEmbedding(
      String photoId, float[] embedding, String caption, String userId, List<String> tags);

  List<Map<String, Object>> searchSimilarPhotos(float[] embedding, int limit, String excludedId);

  void deletePhotoEmbedding(String photoId);

  boolean hasPhotoEmbedding(String photoId);
}
