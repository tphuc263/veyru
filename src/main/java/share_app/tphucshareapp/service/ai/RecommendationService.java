package share_app.tphucshareapp.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.dto.response.recommendation.RecommendedUserResponse;
import share_app.tphucshareapp.model.Favorite;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.FavoriteRepository;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.service.photo.PhotoConversionService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Recommendation Service using Redis Vector Search with Gemini embeddings.
 * <p>
 * - Related Posts: given a photo, find the most similar photos by embedding cosine similarity.
 * - Suggested Users: given a user, find users with similar interest profiles.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final EmbeddingService embeddingService;
    private final RedisVectorService redisVectorService;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final PhotoConversionService photoConversionService;

    // ─── RELATED POSTS ─────────────────────────────────────────────

    /**
     * Get photos similar to the given photo using vector similarity.
     * Falls back to tag-based matching if embedding is unavailable.
     */
    public List<PhotoResponse> getRelatedPhotos(String photoId, int limit, User currentUser) {
        log.info("Getting related photos for photoId: {}, limit: {}", photoId, limit);

        Photo sourcePhoto = photoRepository.findById(photoId).orElse(null);
        if (sourcePhoto == null) {
            log.warn("Source photo not found: {}", photoId);
            return Collections.emptyList();
        }

        // Try vector similarity search first
        try {
            // Ensure source photo has an embedding
            ensurePhotoEmbedding(sourcePhoto);

            // Build query embedding from this photo
            String photoText = embeddingService.buildPhotoText(sourcePhoto.getCaption(), sourcePhoto.getTags());
            float[] queryEmbedding = embeddingService.generateEmbedding(photoText);

            if (queryEmbedding != null) {
                List<Map<String, Object>> results = redisVectorService.searchSimilarPhotos(queryEmbedding, limit, photoId);

                if (!results.isEmpty()) {
                    List<String> photoIds = results.stream()
                            .map(r -> (String) r.get("entityId"))
                            .filter(Objects::nonNull)
                            .toList();

                    List<Photo> photos = photoRepository.findAllById(photoIds);

                    // Maintain order from vector search
                    Map<String, Photo> photoMap = photos.stream().collect(Collectors.toMap(Photo::getId, p -> p));
                    List<PhotoResponse> responses = new ArrayList<>();
                    for (String pid : photoIds) {
                        Photo p = photoMap.get(pid);
                        if (p != null) {
                            responses.add(photoConversionService.convertToPhotoResponse(p, currentUser));
                        }
                    }
                    log.info("Found {} related photos via vector search for {}", responses.size(), photoId);
                    return responses;
                }
            }
        } catch (Exception e) {
            log.warn("Vector search failed for related photos, falling back to tags: {}", e.getMessage());
        }

        // Fallback: tag-based matching
        return getRelatedPhotosByTags(sourcePhoto, limit, currentUser);
    }

    /**
     * Fallback: find related photos by shared tags.
     */
    private List<PhotoResponse> getRelatedPhotosByTags(Photo sourcePhoto, int limit, User currentUser) {
        if (sourcePhoto.getTags() == null || sourcePhoto.getTags().isEmpty()) {
            return Collections.emptyList();
        }

        List<Photo> allByTags = photoRepository.findByTagsIn(
                        sourcePhoto.getTags(),
                        org.springframework.data.domain.PageRequest.of(0, limit + 1))
                .getContent();

        return allByTags.stream()
                .filter(p -> !p.getId().equals(sourcePhoto.getId()))
                .limit(limit)
                .map(p -> photoConversionService.convertToPhotoResponse(p, currentUser))
                .toList();
    }

    // ─── SUGGESTED USERS ───────────────────────────────────────────

    /**
     * Get user suggestions based on interest similarity.
     * Builds a user profile embedding from their content and engagement,
     * then finds the closest users who the current user does NOT follow.
     */
    public List<RecommendedUserResponse> getSuggestedUsers(String userId, int limit) {
        log.info("Getting suggested users for userId: {}, limit: {}", userId, limit);

        User currentUser = userRepository.findById(userId).orElse(null);
        if (currentUser == null) {
            return Collections.emptyList();
        }

        // Try vector similarity
        try {
            // Ensure current user has an embedding
            ensureUserEmbedding(currentUser);

            // Build current user's profile embedding
            float[] userEmbedding = buildAndGetUserEmbedding(currentUser);

            if (userEmbedding != null) {
                // Search for similar users (fetch extra to filter out already-followed)
                List<Map<String, Object>> results = redisVectorService.searchSimilarUsers(
                        userEmbedding, limit + 20, userId);

                if (!results.isEmpty()) {
                    // Get already-followed user IDs
                    Set<String> followingIds = new HashSet<>();
                    if (currentUser.getFollowingIds() != null) {
                        followingIds.addAll(currentUser.getFollowingIds());
                    }

                    List<RecommendedUserResponse> suggestions = new ArrayList<>();
                    for (Map<String, Object> result : results) {
                        String candidateId = (String) result.get("entityId");
                        if (candidateId == null || followingIds.contains(candidateId)) {
                            continue;
                        }

                        User candidate = userRepository.findById(candidateId).orElse(null);
                        if (candidate == null) continue;

                        double score = result.containsKey("score") ? ((Number) result.get("score")).doubleValue() : 0.0;

                        RecommendedUserResponse resp = new RecommendedUserResponse();
                        resp.setId(candidate.getId());
                        resp.setUsername(candidate.getUsername());
                        resp.setImageUrl(candidate.getImageUrl());
                        resp.setBio(candidate.getBio());
                        resp.setFollowerCount(candidate.getFollowerCount());
                        resp.setPhotoCount(candidate.getPhotoCount());
                        resp.setSimilarityScore(1.0 - score); // Convert distance to similarity
                        resp.setReason(generateRecommendationReason(currentUser, candidate));

                        suggestions.add(resp);
                        if (suggestions.size() >= limit) break;
                    }

                    log.info("Found {} suggested users via vector search for {}", suggestions.size(), userId);
                    return suggestions;
                }
            }
        } catch (Exception e) {
            log.warn("Vector search failed for user suggestions, falling back: {}", e.getMessage());
        }

        // Fallback: suggest popular users not followed
        return getFallbackSuggestedUsers(currentUser, limit);
    }

    /**
     * Fallback: suggest popular users that the current user doesn't follow.
     */
    private List<RecommendedUserResponse> getFallbackSuggestedUsers(User currentUser, int limit) {
        Set<String> followingIds = new HashSet<>();
        followingIds.add(currentUser.getId());
        if (currentUser.getFollowingIds() != null) {
            followingIds.addAll(currentUser.getFollowingIds());
        }

        // Get all users, sort by follower count, exclude followed
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(u -> !followingIds.contains(u.getId()))
                .sorted(Comparator.comparingLong(User::getFollowerCount).reversed())
                .limit(limit)
                .map(u -> {
                    RecommendedUserResponse resp = new RecommendedUserResponse();
                    resp.setId(u.getId());
                    resp.setUsername(u.getUsername());
                    resp.setImageUrl(u.getImageUrl());
                    resp.setBio(u.getBio());
                    resp.setFollowerCount(u.getFollowerCount());
                    resp.setPhotoCount(u.getPhotoCount());
                    resp.setSimilarityScore(0.0);
                    resp.setReason("Popular on Share App");
                    return resp;
                })
                .toList();
    }

    /**
     * Generate a human-readable reason for the recommendation.
     */
    private String generateRecommendationReason(User currentUser, User candidate) {
        // Check for mutual followers
        if (currentUser.getFollowingIds() != null && candidate.getFollowingIds() != null) {
            Set<String> commonFollowings = new HashSet<>(currentUser.getFollowingIds());
            commonFollowings.retainAll(candidate.getFollowingIds());
            if (!commonFollowings.isEmpty()) {
                return "Followed by people you follow";
            }
        }

        // Check for similar content interests
        List<Photo> candidatePhotos = photoRepository.findByUser_UserIdOrderByCreatedAtDesc(candidate.getId());
        List<Photo> currentUserPhotos = photoRepository.findByUser_UserIdOrderByCreatedAtDesc(currentUser.getId());

        if (!candidatePhotos.isEmpty() && !currentUserPhotos.isEmpty()) {
            Set<String> candidateTags = candidatePhotos.stream()
                    .filter(p -> p.getTags() != null)
                    .flatMap(p -> p.getTags().stream())
                    .collect(Collectors.toSet());
            Set<String> userTags = currentUserPhotos.stream()
                    .filter(p -> p.getTags() != null)
                    .flatMap(p -> p.getTags().stream())
                    .collect(Collectors.toSet());
            candidateTags.retainAll(userTags);
            if (!candidateTags.isEmpty()) {
                return "Similar interests in " + candidateTags.stream().limit(2).collect(Collectors.joining(", "));
            }
        }

        return "Suggested for you";
    }

    // ─── EMBEDDING MANAGEMENT ──────────────────────────────────────

    /**
     * Ensure a photo has an embedding stored in Redis.
     * If not, generate and store it.
     */
    public void ensurePhotoEmbedding(Photo photo) {
        if (redisVectorService.hasPhotoEmbedding(photo.getId())) {
            return;
        }
        String text = embeddingService.buildPhotoText(photo.getCaption(), photo.getTags());
        if (text.isBlank()) return;

        float[] embedding = embeddingService.generateEmbedding(text);
        if (embedding != null) {
            String userId = photo.getUser() != null ? photo.getUser().getUserId() : "";
            redisVectorService.storePhotoEmbedding(photo.getId(), embedding, photo.getCaption(), userId, photo.getTags());
        }
    }

    /**
     * Ensure a user has a profile embedding stored in Redis.
     */
    public void ensureUserEmbedding(User user) {
        if (redisVectorService.hasUserEmbedding(user.getId())) {
            return;
        }
        buildAndStoreUserEmbedding(user);
    }

    /**
     * Build and store a user embedding from their profile + content + engagement.
     */
    private float[] buildAndGetUserEmbedding(User user) {
        // Collect user's tags from their photos
        List<Photo> userPhotos = photoRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getId());
        List<String> topTags = userPhotos.stream()
                .filter(p -> p.getTags() != null)
                .flatMap(p -> p.getTags().stream())
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(20)
                .map(Map.Entry::getKey)
                .toList();

        // Collect captions from liked/favorited photos for interest signal
        List<Favorite> favorites = favoriteRepository.findByUserId(user.getId());
        List<String> favoriteCaptions = new ArrayList<>();
        if (!favorites.isEmpty()) {
            List<String> favPhotoIds = favorites.stream()
                    .map(Favorite::getPhotoId)
                    .limit(10)
                    .toList();
            List<Photo> favPhotos = photoRepository.findAllById(favPhotoIds);
            favoriteCaptions = favPhotos.stream()
                    .map(Photo::getCaption)
                    .filter(c -> c != null && !c.isBlank())
                    .toList();
        }

        // Also add recent captions from the user's photos
        List<String> recentCaptions = userPhotos.stream()
                .limit(5)
                .map(Photo::getCaption)
                .filter(c -> c != null && !c.isBlank())
                .collect(Collectors.toList());
        recentCaptions.addAll(favoriteCaptions);

        String profileText = embeddingService.buildUserProfileText(user.getBio(), topTags, recentCaptions);
        if (profileText.isBlank()) return null;

        return embeddingService.generateEmbedding(profileText);
    }

    private void buildAndStoreUserEmbedding(User user) {
        float[] embedding = buildAndGetUserEmbedding(user);
        if (embedding != null) {
            redisVectorService.storeUserEmbedding(user.getId(), embedding, user.getUsername(), user.getBio());
        }
    }

    // ─── BATCH / EVENT-DRIVEN INDEXING ─────────────────────────────

    /**
     * Index a newly created photo (called from PhotoCreatedEvent listener).
     */
    @Async
    public void indexNewPhoto(String photoId) {
        try {
            Photo photo = photoRepository.findById(photoId).orElse(null);
            if (photo == null) return;
            ensurePhotoEmbedding(photo);
            log.info("Indexed new photo embedding: {}", photoId);
        } catch (Exception e) {
            log.error("Failed to index new photo {}: {}", photoId, e.getMessage());
        }
    }

    /**
     * Re-index a user's profile embedding (e.g., after they post, like, or update profile).
     */
    @Async
    public void reindexUser(String userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return;
            // Force re-build by deleting existing
            redisVectorService.deleteUserEmbedding(userId);
            buildAndStoreUserEmbedding(user);
            log.info("Re-indexed user embedding: {}", userId);
        } catch (Exception e) {
            log.error("Failed to re-index user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Batch index all existing photos (admin/init operation).
     */
    public int batchIndexAllPhotos() {
        log.info("Starting batch indexing of all photos...");
        List<Photo> allPhotos = photoRepository.findAll();
        int indexed = 0;
        for (Photo photo : allPhotos) {
            try {
                ensurePhotoEmbedding(photo);
                indexed++;
            } catch (Exception e) {
                log.warn("Failed to index photo {}: {}", photo.getId(), e.getMessage());
            }
        }
        log.info("Batch indexed {}/{} photos", indexed, allPhotos.size());
        return indexed;
    }

    /**
     * Batch index all existing users (admin/init operation).
     */
    public int batchIndexAllUsers() {
        log.info("Starting batch indexing of all users...");
        List<User> allUsers = userRepository.findAll();
        int indexed = 0;
        for (User user : allUsers) {
            try {
                ensureUserEmbedding(user);
                indexed++;
            } catch (Exception e) {
                log.warn("Failed to index user {}: {}", user.getId(), e.getMessage());
            }
        }
        log.info("Batch indexed {}/{} users", indexed, allUsers.size());
        return indexed;
    }
}
