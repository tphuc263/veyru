package share_app.tphucshareapp.service.photo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.dto.response.post.UnifiedPostResponse;
import share_app.tphucshareapp.model.Follow;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.FollowRepository;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.repository.LikeRepository;
import share_app.tphucshareapp.repository.FavoriteRepository;
import share_app.tphucshareapp.service.share.ShareService;
import share_app.tphucshareapp.service.user.UserService;
import share_app.tphucshareapp.service.user.UserAvatarCacheService;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsfeedService implements INewsfeedService {

    private final FollowRepository followRepository;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PhotoConversionService photoConversionService;
    private final UserService userService;
    private final ShareService shareService;
    private final UserAvatarCacheService userAvatarCacheService;
    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;

    // Cache configuration
    private static final String NEWSFEED_CACHE_KEY = "newsfeed:user:";
    private static final Duration CACHE_TTL = Duration.ofHours(2);
    private static final int MAX_CACHED_ITEMS = 200;

    @Override
    public Page<PhotoResponse> getNewsfeed(String userId, int page, int size) {
        User currentUser = userService.findUserById(userId);

        // Get users that current user follows
        List<String> followingIds = new ArrayList<>(getFollowingUserIds(userId));
        
        // If not following anyone, return user's own photos
        if (followingIds.isEmpty()) {
            log.info("User {} is not following anyone, showing own photos", userId);
            List<Photo> userPhotos = photoRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
            return paginateAndConvert(userPhotos, currentUser, PageRequest.of(page, size));
        }

        // Include user's own photos in feed
        followingIds.add(userId);

        // Fetch recent photos from followed users (last 30 days)
        Instant cutoffTime = Instant.now().minus(Duration.ofDays(30));
        List<Photo> photos = photoRepository.findByUser_UserIdInAndCreatedAtAfterOrderByCreatedAtDesc(
            followingIds, cutoffTime
        );
        
        // If no recent photos, get all photos from followed users
        if (photos.isEmpty()) {
            log.info("No recent photos found, fetching all photos from followed users");
            photos = photoRepository.findByUser_UserIdInOrderByCreatedAtDesc(followingIds);
        }

        // Apply simple ranking by engagement and recency
        List<Photo> rankedPhotos = rankPhotos(photos);

        // Convert to response and paginate
        return paginateAndConvert(rankedPhotos, currentUser, PageRequest.of(page, size));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<PhotoResponse> getCachedNewsfeed(String userId, int page, int size) {
        User currentUser = userService.findUserById(userId);

        String cacheKey = NEWSFEED_CACHE_KEY + userId;

        try {
            // Get cached photo IDs
            List<String> cachedPhotoIds = (List<String>) redisTemplate.opsForValue().get(cacheKey);

            if (cachedPhotoIds == null || cachedPhotoIds.isEmpty()) {
                log.info("No cached feed found for user: {}, generating new one", userId);
                generateNewsfeedCache(userId);
                cachedPhotoIds = (List<String>) redisTemplate.opsForValue().get(cacheKey);
            }

            if (cachedPhotoIds == null || cachedPhotoIds.isEmpty()) {
                return Page.empty();
            }

            // Paginate cached IDs
            Pageable pageable = PageRequest.of(page, size);
            int start = (int) pageable.getOffset();
            int end = Math.min(start + size, cachedPhotoIds.size());

            if (start >= cachedPhotoIds.size()) {
                return Page.empty();
            }

            List<String> pagePhotoIds = cachedPhotoIds.subList(start, end);

            // Fetch photos and convert to response
            List<Photo> photos = photoRepository.findAllById(pagePhotoIds);

            List<PhotoResponse> photoResponses = photos.stream()
                    .map(photo -> photoConversionService.convertToPhotoResponse(photo, currentUser))
                    .toList();

            // Maintain order from cache
            photoResponses.sort((a, b) ->
                    Integer.compare(pagePhotoIds.indexOf(a.getId()), pagePhotoIds.indexOf(b.getId())));

            return new PageImpl<>(photoResponses, pageable, cachedPhotoIds.size());

        } catch (Exception e) {
            log.error("Error retrieving cached newsfeed for user: {}", userId, e);
            // Fallback to real-time generation
            return getNewsfeed(userId, page, size);
        }
    }

    @Override
    public void generateNewsfeedCache(String userId) {
        log.info("Generating newsfeed cache for user: {}", userId);

        try {
            // Generate feed using real-time algorithm
            List<String> followingIds = new ArrayList<>(getFollowingUserIds(userId));
            if (followingIds.isEmpty()) {
                return;
            }

            // Include user's own photos in feed
            followingIds.add(userId);
            
            // Fetch recent photos
            Instant cutoffTime = Instant.now().minus(Duration.ofDays(30));
            List<Photo> candidatePhotos = photoRepository.findByUser_UserIdInAndCreatedAtAfterOrderByCreatedAtDesc(
                followingIds, cutoffTime
            );
            
            List<Photo> rankedPhotos = rankPhotos(candidatePhotos);

            // Limit cache size for performance
            List<String> photoIds = rankedPhotos.stream()
                    .limit(MAX_CACHED_ITEMS)
                    .map(Photo::getId)
                    .collect(Collectors.toList());

            // Store in cache
            String cacheKey = NEWSFEED_CACHE_KEY + userId;
            redisTemplate.opsForValue().set(cacheKey, photoIds, CACHE_TTL);

            log.info("Cached {} photos for user: {}", photoIds.size(), userId);

        } catch (Exception e) {
            log.error("Error generating newsfeed cache for user: {}", userId, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateFollowersFeeds(String photoId, String authorId) {
        log.info("Updating followers' feeds with new photo: {} from author: {}", photoId, authorId);

        try {
            // Get all followers of the photo author
            List<Follow> followers = followRepository.findByFollowingId(authorId);

            for (Follow follow : followers) {
                String followerId = follow.getFollowerId();
                String cacheKey = NEWSFEED_CACHE_KEY + followerId;

                // Get current cached feed
                List<String> cachedPhotoIds = (List<String>) redisTemplate.opsForValue().get(cacheKey);

                if (cachedPhotoIds != null) {
                    // Add new photo to the beginning of feed
                    List<String> updatedFeed = new ArrayList<>();
                    updatedFeed.add(photoId);
                    updatedFeed.addAll(cachedPhotoIds);

                    // Limit size
                    if (updatedFeed.size() > MAX_CACHED_ITEMS) {
                        updatedFeed = updatedFeed.subList(0, MAX_CACHED_ITEMS);
                    }

                    // Update cache
                    redisTemplate.opsForValue().set(cacheKey, updatedFeed, CACHE_TTL);
                }
            }

            log.info("Updated feeds for {} followers", followers.size());

        } catch (Exception e) {
            log.error("Error updating followers' feeds for photo: {}", photoId, e);
        }
    }

    @Override
    public Page<PhotoResponse> getSmartNewsfeed(String userId, int page, int size) {
        log.info("Getting smart newsfeed for user: {}", userId);

        // Use simple real-time generation for now
        // This is more reliable and easier to debug
        return getNewsfeed(userId, page, size);
    }

    @Override
    public Page<UnifiedPostResponse> getUnifiedNewsfeed(String userId, int page, int size) {
        log.info("Getting unified newsfeed (photos + shares) for user: {}", userId);

        User currentUser = userService.findUserById(userId);

        // Get following user IDs
        List<String> followingIds = new ArrayList<>(getFollowingUserIds(userId));

        // Include user's own content
        followingIds.add(userId);

        // Get photos from followed users
        List<Photo> photos = new ArrayList<>();
        if (!followingIds.isEmpty()) {
            Instant cutoffTime = Instant.now().minus(Duration.ofDays(30));
            photos = photoRepository.findByUser_UserIdInAndCreatedAtAfterOrderByCreatedAtDesc(
                followingIds, cutoffTime
            );
            if (photos.isEmpty()) {
                photos = photoRepository.findByUser_UserIdInOrderByCreatedAtDesc(followingIds);
            }
        }

        // Get shares from followed users
        List<share_app.tphucshareapp.model.Share> shares = new ArrayList<>();
        if (!followingIds.isEmpty()) {
            shares = shareService.getSharesByUserIds(followingIds);
        }

        // Combine and sort by createdAt
        List<UnifiedPostResponse> unifiedPosts = new ArrayList<>();

        // Add photos
        for (Photo photo : photos) {
            UnifiedPostResponse post = new UnifiedPostResponse();
            post.setId(photo.getId());
            post.setType(UnifiedPostResponse.PostType.PHOTO);
            post.setCreatedAt(photo.getCreatedAt());
            post.setUserId(photo.getUser().getUserId());
            post.setUsername(photo.getUser().getUsername());
            // Use userAvatarCacheService to get avatar
            String avatarUrl = photo.getUser() != null ?
                userAvatarCacheService.getAvatar(photo.getUser().getUserId()) : null;
            post.setUserImageUrl(avatarUrl);
            post.setImageUrl(photo.getImageUrl());
            post.setCaption(photo.getCaption());
            post.setLikeCount((int) photo.getLikeCount());
            post.setCommentCount((int) photo.getCommentCount());
            post.setShareCount((int) photo.getShareCount());
            // Check like/save status
            boolean isLiked = likeRepository.existsByPhotoIdAndUserId(photo.getId(), currentUser.getId());
            boolean isSaved = favoriteRepository.existsByUserIdAndPhotoId(currentUser.getId(), photo.getId());
            post.setLikedByCurrentUser(isLiked);
            post.setSavedByCurrentUser(isSaved);
            unifiedPosts.add(post);
        }

        // Add shares
        for (share_app.tphucshareapp.model.Share share : shares) {
            Photo originalPhoto = photoRepository.findById(share.getPhotoId()).orElse(null);
            UnifiedPostResponse post = new UnifiedPostResponse();
            post.setId("share_" + share.getId());
            post.setType(UnifiedPostResponse.PostType.SHARE);
            post.setCreatedAt(share.getCreatedAt());
            post.setUserId(share.getUserId());
            // Get user info from repository
            User shareUser = userRepository.findById(share.getUserId()).orElse(null);
            if (shareUser != null) {
                post.setUsername(shareUser.getUsername());
                post.setUserImageUrl(shareUser.getImageUrl());
            }
            post.setShareCaption(share.getCaption());
            post.setLikeCount(0);
            post.setCommentCount(0);
            post.setShareCount(0);
            post.setLikedByCurrentUser(false);
            post.setSavedByCurrentUser(false);

            // Original photo info
            if (originalPhoto != null) {
                post.setOriginalPhotoId(originalPhoto.getId());
                post.setOriginalImageUrl(originalPhoto.getImageUrl());
                post.setOriginalCaption(originalPhoto.getCaption());
                if (originalPhoto.getUser() != null) {
                    post.setOriginalUsername(originalPhoto.getUser().getUsername());
                    // Use userAvatarCacheService to get avatar
                    String originalAvatarUrl = userAvatarCacheService.getAvatar(originalPhoto.getUser().getUserId());
                    post.setOriginalUserImageUrl(originalAvatarUrl);
                }
                post.setOriginalCreatedAt(originalPhoto.getCreatedAt());
                post.setOriginalLikeCount((int) originalPhoto.getLikeCount());
                post.setOriginalCommentCount((int) originalPhoto.getCommentCount());
                post.setOriginalShareCount((int) originalPhoto.getShareCount());
            }

            unifiedPosts.add(post);
        }

        // Sort by createdAt descending
        unifiedPosts.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        // Paginate
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), unifiedPosts.size());

        if (start >= unifiedPosts.size()) {
            return new PageImpl<>(List.of(), pageable, unifiedPosts.size());
        }

        List<UnifiedPostResponse> pagePosts = unifiedPosts.subList(start, end);
        return new PageImpl<>(pagePosts, pageable, unifiedPosts.size());
    }

    private List<String> getFollowingUserIds(String userId) {
        List<Follow> following = followRepository.findByFollowerId(userId);
        return following.stream()
                .map(Follow::getFollowingId)
                .toList();
    }

    private List<Photo> rankPhotos(List<Photo> photos) {
        return photos.stream()
                .map(photo -> new PhotoWithScore(photo, calculateRelevantScore(photo)))
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .map(photoWithScore -> photoWithScore.photo)
                .toList();
    }

    private double calculateRelevantScore(Photo photo) {
        double score = 0.0;

        // Time decay: newer photos get higher score
        long hoursOld = Duration.between(photo.getCreatedAt(), Instant.now()).toHours();
        
        // Score decreases as photo gets older
        // Recent photos (< 24h): 100-50 points
        // Medium age (24-168h): 50-10 points  
        // Older (> 168h): 10-0 points
        if (hoursOld < 24) {
            score += 100 - (hoursOld * 2);
        } else if (hoursOld < 168) { // 1 week
            score += 50 - ((hoursOld - 24) * 0.3);
        } else {
            score += Math.max(0, 10 - ((hoursOld - 168) * 0.1));
        }

        // Engagement signals
        score += photo.getLikeCount() * 2;  // Each like adds 2 points
        score += photo.getCommentCount() * 5;  // Each comment adds 5 points (more valuable)

        // Content quality signals
        if (photo.getCaption() != null && !photo.getCaption().trim().isEmpty()) {
            score += 10;  // Photos with captions are more engaging
        }
        
        if (photo.getTags() != null && !photo.getTags().isEmpty()) {
            score += 5;  // Tagged photos get small boost
        }

        return score;
    }

    private Page<PhotoResponse> paginateAndConvert(List<Photo> photos, User currentUser, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), photos.size());

        if (start >= photos.size()) {
            return Page.empty();
        }

        List<Photo> pagePhotos = photos.subList(start, end);

        List<PhotoResponse> photoResponses = pagePhotos.stream()
                .map(photo -> photoConversionService.convertToPhotoResponse(photo, currentUser))
                .toList();

        return new PageImpl<>(photoResponses, pageable, photos.size());
    }

    // helper class for ranking
    private static class PhotoWithScore {
        final Photo photo;
        final double score;

        PhotoWithScore(Photo photo, double score) {
            this.photo = photo;
            this.score = score;
        }
    }
}