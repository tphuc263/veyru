package com.veyru.adapter.out.redis;

import com.veyru.application.intelligence.EmbeddingService;
import com.veyru.application.port.out.VectorIndex;
import io.lettuce.core.RedisCommandExecutionException;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Manages the Redis Search vector index for photo embeddings. Uses Redis Stack RediSearch FT.CREATE
 * with HNSW algorithm for fast ANN queries.
 */
@Service
public class RedisVectorAdapter implements VectorIndex {
  private static final Logger log = LoggerFactory.getLogger(RedisVectorAdapter.class);
  private final RedisTemplate<String, Object> redisTemplate;
  // Redis key prefixes
  public static final String PHOTO_PREFIX = "photo_vec:";
  // Index names
  public static final String PHOTO_INDEX = "photo_vec_idx";
  private static final int VECTOR_DIM = EmbeddingService.EMBEDDING_DIMENSION;

  @PostConstruct
  public void initializeIndexes() {
    try {
      createIndexIfNotExists(
          PHOTO_INDEX,
          PHOTO_PREFIX,
          new String[] {"caption", "TAG", "userId", "TAG", "tags", "TAG"});
      log.info("Redis photo vector index initialized successfully");
    } catch (RuntimeException exception) {
      log.warn("Redis Search unavailable; related photos will use tag matching", exception);
    }
  }

  /** Create a vector search index if it doesn't exist. */
  private void createIndexIfNotExists(String indexName, String prefix, String[] extraFields) {
    try {
      redisTemplate.execute(
          (RedisConnection connection) -> {
            createVectorIndex(connection, indexName, prefix, extraFields);
            return null;
          });
    } catch (RedisSystemException e) {
      if (e.getCause() instanceof RedisCommandExecutionException cause
          && "Index already exists".equals(cause.getMessage())) {
        log.info("Index '{}' already exists", indexName);
        return;
      }
      throw e;
    }
  }

  private void createVectorIndex(
      RedisConnection connection, String indexName, String prefix, String[] extraFields) {
    // Build FT.CREATE command
    // FT.CREATE {idx} ON HASH PREFIX 1 {prefix} SCHEMA
    //   embedding VECTOR HNSW 6 TYPE FLOAT32 DIM {VECTOR_DIM} DISTANCE_METRIC COSINE
    //   {extra fields...}
    List<byte[]> args = new ArrayList<>();
    args.add(indexName.getBytes(StandardCharsets.UTF_8));
    args.add("ON".getBytes(StandardCharsets.UTF_8));
    args.add("HASH".getBytes(StandardCharsets.UTF_8));
    args.add("PREFIX".getBytes(StandardCharsets.UTF_8));
    args.add("1".getBytes(StandardCharsets.UTF_8));
    args.add(prefix.getBytes(StandardCharsets.UTF_8));
    args.add("SCHEMA".getBytes(StandardCharsets.UTF_8));
    // Embedding vector field
    args.add("embedding".getBytes(StandardCharsets.UTF_8));
    args.add("VECTOR".getBytes(StandardCharsets.UTF_8));
    args.add("HNSW".getBytes(StandardCharsets.UTF_8));
    args.add("6".getBytes(StandardCharsets.UTF_8)); // 6 args follow
    args.add("TYPE".getBytes(StandardCharsets.UTF_8));
    args.add("FLOAT32".getBytes(StandardCharsets.UTF_8));
    args.add("DIM".getBytes(StandardCharsets.UTF_8));
    args.add(String.valueOf(VECTOR_DIM).getBytes(StandardCharsets.UTF_8));
    args.add("DISTANCE_METRIC".getBytes(StandardCharsets.UTF_8));
    args.add("COSINE".getBytes(StandardCharsets.UTF_8));
    // Extra fields (e.g., caption, userId, tags)
    for (String field : extraFields) {
      args.add(field.getBytes(StandardCharsets.UTF_8));
    }
    connection.execute("FT.CREATE", args.toArray(new byte[0][]));
    log.info("Created vector index: {}", indexName);
  }

  /** Store a photo embedding in Redis. */
  public void storePhotoEmbedding(
      String photoId, float[] embedding, String caption, String userId, List<String> tags) {
    String key = PHOTO_PREFIX + photoId;

    Map<byte[], byte[]> hash = new HashMap<>();
    hash.put(
        "embedding".getBytes(StandardCharsets.UTF_8),
        EmbeddingService.floatArrayToBytes(embedding));
    hash.put(
        "caption".getBytes(StandardCharsets.UTF_8),
        (caption != null ? caption : "").getBytes(StandardCharsets.UTF_8));
    hash.put(
        "userId".getBytes(StandardCharsets.UTF_8),
        (userId != null ? userId : "").getBytes(StandardCharsets.UTF_8));
    hash.put(
        "tags".getBytes(StandardCharsets.UTF_8),
        (tags != null ? String.join(",", tags) : "").getBytes(StandardCharsets.UTF_8));
    hash.put("photoId".getBytes(StandardCharsets.UTF_8), photoId.getBytes(StandardCharsets.UTF_8));
    redisTemplate.execute(
        (RedisConnection connection) -> {
          connection.hashCommands().hMSet(key.getBytes(StandardCharsets.UTF_8), hash);
          return null;
        });
    log.debug("Stored photo embedding for photoId: {}", photoId);
  }

  /**
   * Search for similar photos using KNN vector search.
   *
   * @param queryEmbedding the query vector
   * @param topK number of results
   * @param excludePhotoId photo ID to exclude from results (e.g., the source photo)
   * @return list of {photoId, score} maps ordered by similarity
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> searchSimilarPhotos(
      float[] queryEmbedding, int topK, String excludePhotoId) {

    // FT.SEARCH photo_vec_idx "*=>[KNN {topK} @embedding $query_vec AS score]"
    //   PARAMS 2 query_vec {blob} SORTBY score DIALECT 2
    byte[] vectorBytes = EmbeddingService.floatArrayToBytes(queryEmbedding);
    int searchK = topK + 5; // fetch extra to account for exclusions
    String queryStr = String.format("*=>[KNN %d @embedding $query_vec AS score]", searchK);
    List<byte[]> args = new ArrayList<>();
    args.add(PHOTO_INDEX.getBytes(StandardCharsets.UTF_8));
    args.add(queryStr.getBytes(StandardCharsets.UTF_8));
    args.add("PARAMS".getBytes(StandardCharsets.UTF_8));
    args.add("2".getBytes(StandardCharsets.UTF_8));
    args.add("query_vec".getBytes(StandardCharsets.UTF_8));
    args.add(vectorBytes);
    args.add("SORTBY".getBytes(StandardCharsets.UTF_8));
    args.add("score".getBytes(StandardCharsets.UTF_8));
    args.add("DIALECT".getBytes(StandardCharsets.UTF_8));
    args.add("2".getBytes(StandardCharsets.UTF_8));
    List<Object> rawResult =
        (List<Object>)
            redisTemplate.execute(
                (RedisConnection connection) ->
                    connection.execute("FT.SEARCH", args.toArray(new byte[0][])));
    return parseSearchResults(rawResult, "photoId", excludePhotoId, topK);
  }

  /** Delete a photo embedding from Redis. */
  public void deletePhotoEmbedding(String photoId) {

    redisTemplate.delete(PHOTO_PREFIX + photoId);
  }

  /** Check if a photo embedding exists. */
  public boolean hasPhotoEmbedding(String photoId) {
    Boolean exists = redisTemplate.hasKey(PHOTO_PREFIX + photoId);
    return Boolean.TRUE.equals(exists);
  }

  /**
   * Parse FT.SEARCH result into a clean list of maps. Redis FT.SEARCH returns: [totalCount, key1,
   * [field1, val1, ...], key2, [field2, val2, ...], ...]
   */
  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> parseSearchResults(
      List<Object> rawResult, String idField, String excludeId, int topK) {
    if (rawResult == null || rawResult.size() < 2) {
      return Collections.emptyList();
    }
    List<Map<String, Object>> results = new ArrayList<>();
    // rawResult[0] = total count (Long)
    // rawResult[1] = key, rawResult[2] = [field, value, ...], etc.
    for (int i = 1; i < rawResult.size() - 1; i += 2) {
      String redisKey = parseRedisValue(rawResult.get(i));
      List<Object> fields = (List<Object>) rawResult.get(i + 1);
      if (fields == null) continue;
      Map<String, Object> doc = new HashMap<>();
      for (int j = 0; j < fields.size() - 1; j += 2) {
        String fieldName = parseRedisValue(fields.get(j));
        String fieldValue = parseRedisValue(fields.get(j + 1));
        doc.put(fieldName, fieldValue);
      }
      // Extract the entity ID
      String entityId = (String) doc.get(idField);
      if (entityId == null) {
        // Fallback: extract from Redis key (photo_vec:xxx)
        if (redisKey.contains(":")) {
          entityId = redisKey.substring(redisKey.indexOf(":") + 1);
        }
      }
      // Skip excluded ID
      if (excludeId != null && excludeId.equals(entityId)) {
        continue;
      }
      doc.put("entityId", entityId);
      // Parse score (lower = more similar for COSINE distance)
      if (doc.containsKey("score")) {
        try {
          doc.put("score", Double.parseDouble((String) doc.get("score")));
        } catch (NumberFormatException e) {
          doc.put("score", 1.0);
        }
      }
      results.add(doc);
      if (results.size() >= topK) break;
    }
    return results;
  }

  private String parseRedisValue(Object value) {
    if (value == null) return "";
    if (value instanceof byte[]) {
      return new String((byte[]) value, StandardCharsets.UTF_8);
    }
    return value.toString();
  }

  public RedisVectorAdapter(final RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }
}
