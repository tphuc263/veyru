package share_app.tphucshareapp.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.repository.UserRepository;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAvatarCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;

    private static final String AVATAR_KEY_PREFIX = "user:avatar:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    public String getAvatar(String userId) {
        if (userId == null) return null;

        String key = AVATAR_KEY_PREFIX + userId;
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return cached.toString();
            }
        } catch (Exception e) {
            log.debug("Redis cache miss for avatar, userId: {}", userId);
        }

        // Fallback to DB
        return userRepository.findById(userId)
                .map(user -> {
                    String imageUrl = user.getImageUrl();
                    if (imageUrl != null) {
                        try {
                            redisTemplate.opsForValue().set(key, imageUrl, CACHE_TTL);
                        } catch (Exception e) {
                            log.debug("Failed to cache avatar for userId: {}", userId);
                        }
                    }
                    return imageUrl;
                })
                .orElse(null);
    }

    public Map<String, String> getAvatars(List<String> userIds) {
        Map<String, String> result = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) return result;

        List<String> missingIds = new java.util.ArrayList<>();

        // Try cache first
        for (String userId : userIds) {
            String key = AVATAR_KEY_PREFIX + userId;
            try {
                Object cached = redisTemplate.opsForValue().get(key);
                if (cached != null) {
                    result.put(userId, cached.toString());
                } else {
                    missingIds.add(userId);
                }
            } catch (Exception e) {
                missingIds.add(userId);
            }
        }

        // Fetch missing from DB
        if (!missingIds.isEmpty()) {
            userRepository.findAllById(missingIds).forEach(user -> {
                String imageUrl = user.getImageUrl();
                result.put(user.getId(), imageUrl);
                if (imageUrl != null) {
                    try {
                        redisTemplate.opsForValue().set(AVATAR_KEY_PREFIX + user.getId(), imageUrl, CACHE_TTL);
                    } catch (Exception e) {
                        log.debug("Failed to cache avatar for userId: {}", user.getId());
                    }
                }
            });
        }

        return result;
    }

    public void updateAvatar(String userId, String imageUrl) {
        String key = AVATAR_KEY_PREFIX + userId;
        try {
            if (imageUrl != null) {
                redisTemplate.opsForValue().set(key, imageUrl, CACHE_TTL);
            } else {
                redisTemplate.delete(key);
            }
        } catch (Exception e) {
            log.warn("Failed to update avatar cache for userId: {}", userId);
        }
    }

    public void evictAvatar(String userId) {
        try {
            redisTemplate.delete(AVATAR_KEY_PREFIX + userId);
        } catch (Exception e) {
            log.warn("Failed to evict avatar cache for userId: {}", userId);
        }
    }
}
