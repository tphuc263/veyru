package com.veyru.adapter.out.redis;

import com.veyru.application.identity.AuthenticatedUser;
import com.veyru.application.port.out.AuthorizationCodeStore;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class RedisAuthorizationCodeStore implements AuthorizationCodeStore {
  private static final String PREFIX = "auth:oauth-code:";
  private final StringRedisTemplate redis;
  private final ObjectMapper objectMapper;

  public RedisAuthorizationCodeStore(StringRedisTemplate redis, ObjectMapper objectMapper) {
    this.redis = redis;
    this.objectMapper = objectMapper;
  }

  @Override
  public String issue(AuthenticatedUser user, Duration ttl) {
    String code = SecureTokens.random();
    redis.opsForValue().set(key(code), objectMapper.writeValueAsString(user), ttl);
    return code;
  }

  @Override
  public Optional<AuthenticatedUser> consume(String code) {
    if (code == null || code.isBlank()) return Optional.empty();
    String value = redis.opsForValue().getAndDelete(key(code));
    return value == null
        ? Optional.empty()
        : Optional.of(objectMapper.readValue(value, AuthenticatedUser.class));
  }

  private String key(String code) {
    return PREFIX + SecureTokens.hash(code);
  }
}
