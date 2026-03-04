package share_app.tphucshareapp.service.ai;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Manages Redis Search vector indexes for photo and user embeddings.
 * Uses Redis Stack RediSearch FT.CREATE with HNSW algorithm for fast ANN queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisVectorService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Redis key prefixes
    public static final String PHOTO_PREFIX = "photo_vec:";
    public static final String USER_PREFIX = "user_vec:";

    // Index names
    public static final String PHOTO_INDEX = "photo_vec_idx";
    public static final String USER_INDEX = "user_vec_idx";

    private static final int VECTOR_DIM = EmbeddingService.EMBEDDING_DIMENSION; // 768

    @PostConstruct
    public void initializeIndexes() {
        try {
            createIndexIfNotExists(PHOTO_INDEX, PHOTO_PREFIX,
                    new String[]{"caption", "TAG", "userId", "TAG", "tags", "TAG"});
            createIndexIfNotExists(USER_INDEX, USER_PREFIX,
                    new String[]{"username", "TAG", "bio", "TEXT"});
            log.info("Redis vector indexes initialized successfully");
        } catch (Exception e) {
            log.warn("Could not initialize Redis vector indexes (Redis Stack may not be available): {}", e.getMessage());
        }
    }

    /**
     * Create a vector search index if it doesn't exist.
     */
    private void createIndexIfNotExists(String indexName, String prefix, String[] extraFields) {
        try {
            // Check if index exists
            redisTemplate.execute((RedisConnection connection) -> {
                try {
                    connection.execute("FT.INFO", indexName.getBytes(StandardCharsets.UTF_8));
                    log.info("Index '{}' already exists", indexName);
                } catch (Exception e) {
                    // Index doesn't exist, create it
                    log.info("Creating index '{}'", indexName);
                    createVectorIndex(connection, indexName, prefix, extraFields);
                }
                return null;
            });
        } catch (Exception e) {
            log.warn("Failed to check/create index '{}': {}", indexName, e.getMessage());
        }
    }

    private void createVectorIndex(RedisConnection connection, String indexName, String prefix, String[] extraFields) {
        // Build FT.CREATE command
        // FT.CREATE {idx} ON HASH PREFIX 1 {prefix} SCHEMA
        //   embedding VECTOR HNSW 6 TYPE FLOAT32 DIM 768 DISTANCE_METRIC COSINE
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

    /**
     * Store a photo embedding in Redis.
     */
    public void storePhotoEmbedding(String photoId, float[] embedding, String caption, String userId, List<String> tags) {
        String key = PHOTO_PREFIX + photoId;
        try {
            Map<byte[], byte[]> hash = new HashMap<>();
            hash.put("embedding".getBytes(StandardCharsets.UTF_8), EmbeddingService.floatArrayToBytes(embedding));
            hash.put("caption".getBytes(StandardCharsets.UTF_8),
                    (caption != null ? caption : "").getBytes(StandardCharsets.UTF_8));
            hash.put("userId".getBytes(StandardCharsets.UTF_8),
                    (userId != null ? userId : "").getBytes(StandardCharsets.UTF_8));
            hash.put("tags".getBytes(StandardCharsets.UTF_8),
                    (tags != null ? String.join(",", tags) : "").getBytes(StandardCharsets.UTF_8));
            hash.put("photoId".getBytes(StandardCharsets.UTF_8),
                    photoId.getBytes(StandardCharsets.UTF_8));

            redisTemplate.execute((RedisConnection connection) -> {
                connection.hashCommands().hMSet(key.getBytes(StandardCharsets.UTF_8), hash);
                return null;
            });
            log.debug("Stored photo embedding for photoId: {}", photoId);
        } catch (Exception e) {
            log.error("Failed to store photo embedding for {}: {}", photoId, e.getMessage());
        }
    }

    /**
     * Store a user profile embedding in Redis.
     */
    public void storeUserEmbedding(String visitorUserId, float[] embedding, String username, String bio) {
        String key = USER_PREFIX + visitorUserId;
        try {
            Map<byte[], byte[]> hash = new HashMap<>();
            hash.put("embedding".getBytes(StandardCharsets.UTF_8), EmbeddingService.floatArrayToBytes(embedding));
            hash.put("username".getBytes(StandardCharsets.UTF_8),
                    (username != null ? username : "").getBytes(StandardCharsets.UTF_8));
            hash.put("bio".getBytes(StandardCharsets.UTF_8),
                    (bio != null ? bio : "").getBytes(StandardCharsets.UTF_8));
            hash.put("userId".getBytes(StandardCharsets.UTF_8),
                    visitorUserId.getBytes(StandardCharsets.UTF_8));

            redisTemplate.execute((RedisConnection connection) -> {
                connection.hashCommands().hMSet(key.getBytes(StandardCharsets.UTF_8), hash);
                return null;
            });
            log.debug("Stored user embedding for userId: {}", visitorUserId);
        } catch (Exception e) {
            log.error("Failed to store user embedding for {}: {}", visitorUserId, e.getMessage());
        }
    }

    /**
     * Search for similar photos using KNN vector search.
     *
     * @param queryEmbedding the query vector
     * @param topK           number of results
     * @param excludePhotoId photo ID to exclude from results (e.g., the source photo)
     * @return list of {photoId, score} maps ordered by similarity
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchSimilarPhotos(float[] queryEmbedding, int topK, String excludePhotoId) {
        try {
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

            List<Object> rawResult = (List<Object>) redisTemplate.execute((RedisConnection connection) ->
                    connection.execute("FT.SEARCH", args.toArray(new byte[0][])));

            return parseSearchResults(rawResult, "photoId", excludePhotoId, topK);
        } catch (Exception e) {
            log.error("Failed to search similar photos: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Search for similar users using KNN vector search.
     *
     * @param queryEmbedding the query vector
     * @param topK           number of results
     * @param excludeUserId  user ID to exclude (the current user)
     * @return list of {userId, score} maps ordered by similarity
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchSimilarUsers(float[] queryEmbedding, int topK, String excludeUserId) {
        try {
            byte[] vectorBytes = EmbeddingService.floatArrayToBytes(queryEmbedding);
            int searchK = topK + 5;

            String queryStr = String.format("*=>[KNN %d @embedding $query_vec AS score]", searchK);

            List<byte[]> args = new ArrayList<>();
            args.add(USER_INDEX.getBytes(StandardCharsets.UTF_8));
            args.add(queryStr.getBytes(StandardCharsets.UTF_8));
            args.add("PARAMS".getBytes(StandardCharsets.UTF_8));
            args.add("2".getBytes(StandardCharsets.UTF_8));
            args.add("query_vec".getBytes(StandardCharsets.UTF_8));
            args.add(vectorBytes);
            args.add("SORTBY".getBytes(StandardCharsets.UTF_8));
            args.add("score".getBytes(StandardCharsets.UTF_8));
            args.add("DIALECT".getBytes(StandardCharsets.UTF_8));
            args.add("2".getBytes(StandardCharsets.UTF_8));

            List<Object> rawResult = (List<Object>) redisTemplate.execute((RedisConnection connection) ->
                    connection.execute("FT.SEARCH", args.toArray(new byte[0][])));

            return parseSearchResults(rawResult, "userId", excludeUserId, topK);
        } catch (Exception e) {
            log.error("Failed to search similar users: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Delete a photo embedding from Redis.
     */
    public void deletePhotoEmbedding(String photoId) {
        try {
            redisTemplate.delete(PHOTO_PREFIX + photoId);
        } catch (Exception e) {
            log.warn("Failed to delete photo embedding for {}: {}", photoId, e.getMessage());
        }
    }

    /**
     * Delete a user embedding from Redis.
     */
    public void deleteUserEmbedding(String userId) {
        try {
            redisTemplate.delete(USER_PREFIX + userId);
        } catch (Exception e) {
            log.warn("Failed to delete user embedding for {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Check if a photo embedding exists.
     */
    public boolean hasPhotoEmbedding(String photoId) {
        Boolean exists = redisTemplate.hasKey(PHOTO_PREFIX + photoId);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Check if a user embedding exists.
     */
    public boolean hasUserEmbedding(String userId) {
        Boolean exists = redisTemplate.hasKey(USER_PREFIX + userId);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Parse FT.SEARCH result into a clean list of maps.
     * Redis FT.SEARCH returns: [totalCount, key1, [field1, val1, ...], key2, [field2, val2, ...], ...]
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseSearchResults(List<Object> rawResult, String idField,
                                                           String excludeId, int topK) {
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
                // Fallback: extract from Redis key (photo_vec:xxx or user_vec:xxx)
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
}
