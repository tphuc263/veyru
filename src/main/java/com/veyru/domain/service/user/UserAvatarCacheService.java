package com.veyru.domain.service.user;

import com.veyru.application.port.out.UserRepository;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserAvatarCacheService {
  private static final Logger log = LoggerFactory.getLogger(UserAvatarCacheService.class);
  private final RedisTemplate<String, Object> redisTemplate;
  private final UserRepository userRepository;
  private static final String AVATAR_KEY_PREFIX = "user:avatar:";
  private static final Duration CACHE_TTL = Duration.ofHours(24);

  public String getAvatar(String userId) {
    if (userId == null) return null;
    String key = AVATAR_KEY_PREFIX + userId;

    Object cached = redisTemplate.opsForValue().get(key);
    if (cached != null) {
      return cached.toString();
    }

    // Fallback to DB
    return userRepository
        .findById(userId)
        .map(
            user -> {
              String imageUrl = user.getImageUrl();
              if (imageUrl != null) {

                redisTemplate.opsForValue().set(key, imageUrl, CACHE_TTL);
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

      Object cached = redisTemplate.opsForValue().get(key);
      if (cached != null) {
        result.put(userId, cached.toString());
      } else {
        missingIds.add(userId);
      }
    }
    // Fetch missing from DB
    if (!missingIds.isEmpty()) {
      userRepository
          .findAllById(missingIds)
          .forEach(
              user -> {
                String imageUrl = user.getImageUrl();
                result.put(user.getId(), imageUrl);
                if (imageUrl != null) {

                  redisTemplate
                      .opsForValue()
                      .set(AVATAR_KEY_PREFIX + user.getId(), imageUrl, CACHE_TTL);
                }
              });
    }
    return result;
  }

  public void updateAvatar(String userId, String imageUrl) {
    String key = AVATAR_KEY_PREFIX + userId;

    if (imageUrl != null) {
      redisTemplate.opsForValue().set(key, imageUrl, CACHE_TTL);
    } else {
      redisTemplate.delete(key);
    }
  }

  public void evictAvatar(String userId) {

    redisTemplate.delete(AVATAR_KEY_PREFIX + userId);
  }

  public UserAvatarCacheService(
      final RedisTemplate<String, Object> redisTemplate, final UserRepository userRepository) {
    this.redisTemplate = redisTemplate;
    this.userRepository = userRepository;
  }
}
