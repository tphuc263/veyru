package share_app.tphucshareapp.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Service to generate text embeddings using Gemini text-embedding-004.
 * Returns 768-dimensional float vectors for cosine similarity search.
 */
@Service
@Slf4j
public class EmbeddingService {

    private final RestTemplate restTemplate;

    @Value("${ai.gemini.api-key}")
    private String geminiApiKey;

    private static final String EMBEDDING_MODEL = "text-embedding-004";
    private static final String EMBEDDING_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:embedContent?key=%s";

    public static final int EMBEDDING_DIMENSION = 768;

    public EmbeddingService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Generate embedding vector for the given text.
     *
     * @param text the text to embed
     * @return 768-dimensional float array, or null on failure
     */
    @SuppressWarnings("unchecked")
    public float[] generateEmbedding(String text) {
        if (text == null || text.isBlank()) {
            log.warn("Empty text provided for embedding generation");
            return null;
        }

        // Truncate to ~2000 chars to stay within token limits
        String truncated = text.length() > 2000 ? text.substring(0, 2000) : text;

        try {
            String url = String.format(EMBEDDING_URL, EMBEDDING_MODEL, geminiApiKey);

            Map<String, Object> body = Map.of(
                    "model", "models/" + EMBEDDING_MODEL,
                    "content", Map.of("parts", List.of(Map.of("text", truncated)))
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> embeddingObj = (Map<String, Object>) response.getBody().get("embedding");
                if (embeddingObj != null) {
                    List<Number> values = (List<Number>) embeddingObj.get("values");
                    if (values != null && values.size() == EMBEDDING_DIMENSION) {
                        float[] result = new float[EMBEDDING_DIMENSION];
                        for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
                            result[i] = values.get(i).floatValue();
                        }
                        return result;
                    }
                }
            }

            log.error("Unexpected Gemini embedding response: {}", response.getBody());
            return null;

        } catch (Exception e) {
            log.error("Failed to generate embedding for text: {}", truncated.substring(0, Math.min(100, truncated.length())), e);
            return null;
        }
    }

    /**
     * Build a text representation of a photo for embedding.
     */
    public String buildPhotoText(String caption, List<String> tags) {
        StringBuilder sb = new StringBuilder();
        if (caption != null && !caption.isBlank()) {
            sb.append(caption);
        }
        if (tags != null && !tags.isEmpty()) {
            sb.append(" ").append(String.join(" ", tags));
        }
        return sb.toString().trim();
    }

    /**
     * Build a text representation of a user's interests for embedding.
     * Aggregates from their bio, their photo captions/tags, and liked/favorited content.
     */
    public String buildUserProfileText(String bio, List<String> topTags, List<String> recentCaptions) {
        StringBuilder sb = new StringBuilder();
        if (bio != null && !bio.isBlank()) {
            sb.append(bio).append(" ");
        }
        if (topTags != null && !topTags.isEmpty()) {
            sb.append("interests: ").append(String.join(" ", topTags)).append(" ");
        }
        if (recentCaptions != null && !recentCaptions.isEmpty()) {
            // Take first few captions to summarize content style
            recentCaptions.stream().limit(5).forEach(c -> sb.append(c).append(" "));
        }
        return sb.toString().trim();
    }

    /**
     * Convert float[] to byte[] for Redis VECTOR storage (FLOAT32 little-endian).
     */
    public static byte[] floatArrayToBytes(float[] floats) {
        byte[] bytes = new byte[floats.length * 4];
        for (int i = 0; i < floats.length; i++) {
            int bits = Float.floatToIntBits(floats[i]);
            bytes[i * 4]     = (byte) (bits & 0xFF);
            bytes[i * 4 + 1] = (byte) ((bits >> 8) & 0xFF);
            bytes[i * 4 + 2] = (byte) ((bits >> 16) & 0xFF);
            bytes[i * 4 + 3] = (byte) ((bits >> 24) & 0xFF);
        }
        return bytes;
    }

    /**
     * Convert byte[] back to float[] from Redis VECTOR storage (FLOAT32 little-endian).
     */
    public static float[] bytesToFloatArray(byte[] bytes) {
        float[] floats = new float[bytes.length / 4];
        for (int i = 0; i < floats.length; i++) {
            int bits = (bytes[i * 4] & 0xFF)
                    | ((bytes[i * 4 + 1] & 0xFF) << 8)
                    | ((bytes[i * 4 + 2] & 0xFF) << 16)
                    | ((bytes[i * 4 + 3] & 0xFF) << 24);
            floats[i] = Float.intBitsToFloat(bits);
        }
        return floats;
    }
}
