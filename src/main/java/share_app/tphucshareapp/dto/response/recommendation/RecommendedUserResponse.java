package share_app.tphucshareapp.dto.response.recommendation;

import lombok.Data;

/**
 * Response DTO for a recommended user ("Suggested for you").
 */
@Data
public class RecommendedUserResponse {
    private String id;
    private String username;
    private String imageUrl;
    private String bio;
    private long followerCount;
    private long photoCount;
    private double similarityScore;
    private String reason; // e.g., "Similar interests in travel, photography"
}
