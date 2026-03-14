package share_app.tphucshareapp.service.photo;

import org.springframework.data.domain.Page;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.dto.response.post.UnifiedPostResponse;

public interface INewsfeedService {
    /**
     * Get user's newsfeed with real-time generation (Pull Model)
     * Best for users with few follows or when cache is unavailable
     */
    Page<PhotoResponse> getNewsfeed(String userId, int page, int size);

    /**
     * Get user's newsfeed from pre-generated cache (Push Model)
     * Best for active users with many follows
     */
    Page<PhotoResponse> getCachedNewsfeed(String userId, int page, int size);

    /**
     * Generate and cache newsfeed for a user
     * Called periodically or when user follows/unfollows
     */
    void generateNewsfeedCache(String userId);

    /**
     * Update newsfeed cache when new photo is posted
     * Add photo to followers' cached feeds
     */
    void updateFollowersFeeds(String photoId, String authorId);

    /**
     * Hybrid approach - try cache first, fallback to real-time
     */
    Page<PhotoResponse> getSmartNewsfeed(String userId, int page, int size);

    /**
     * Get unified newsfeed (photos + shares) for home feed
     */
    Page<UnifiedPostResponse> getUnifiedNewsfeed(String userId, int page, int size);
}
