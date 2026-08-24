package com.veyru.adapter.out.redis;

import com.veyru.application.identity.AuthenticatedUser;
import com.veyru.application.port.out.RefreshSessionStore;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class RedisRefreshSessionStore implements RefreshSessionStore {
  private static final String TOKEN_PREFIX = "auth:refresh:token:";
  private static final String SPENT_PREFIX = "auth:refresh:spent:";
  private static final String FAMILY_PREFIX = "auth:refresh:family:";
  private static final String REVOKED_PREFIX = "auth:refresh:revoked:";
  private static final DefaultRedisScript<String> ROTATE =
      new DefaultRedisScript<>(
          """
          local current = redis.call('GET', KEYS[1])
          if current then
            if redis.call('EXISTS', KEYS[4]) == 1 then return 'REVOKED' end
            redis.call('DEL', KEYS[1])
            redis.call('SET', KEYS[2], '1', 'EX', ARGV[1])
            redis.call('SET', KEYS[5], current, 'EX', ARGV[1])
            redis.call('SET', KEYS[3], ARGV[2], 'EX', ARGV[1])
            return current
          end
          if redis.call('EXISTS', KEYS[2]) == 1 then
            local active = redis.call('GET', KEYS[3])
            if active then redis.call('DEL', 'auth:refresh:token:' .. active) end
            redis.call('DEL', KEYS[3])
            redis.call('SET', KEYS[4], '1', 'EX', ARGV[1])
            return 'REUSED'
          end
          return 'INVALID'
          """,
          String.class);

  private final StringRedisTemplate redis;
  private final ObjectMapper objectMapper;

  public RedisRefreshSessionStore(StringRedisTemplate redis, ObjectMapper objectMapper) {
    this.redis = redis;
    this.objectMapper = objectMapper;
  }

  @Override
  public RefreshSession create(AuthenticatedUser user, Duration ttl) {
    String familyId = UUID.randomUUID().toString();
    String token = token(familyId);
    String hash = SecureTokens.hash(token);
    redis.opsForValue().set(tokenKey(hash), encode(user, familyId), ttl);
    redis.opsForValue().set(familyKey(familyId), hash, ttl);
    return new RefreshSession(user, token);
  }

  @Override
  public Optional<RefreshSession> rotate(String token, Duration ttl) {
    String familyId = familyId(token);
    if (familyId == null) return Optional.empty();
    String oldHash = SecureTokens.hash(token);
    String nextToken = token(familyId);
    String nextHash = SecureTokens.hash(nextToken);
    String result =
        redis.execute(
            ROTATE,
            List.of(
                tokenKey(oldHash),
                spentKey(oldHash),
                familyKey(familyId),
                revokedKey(familyId),
                tokenKey(nextHash)),
            Long.toString(ttl.toSeconds()),
            nextHash);
    if (result == null
        || result.equals("INVALID")
        || result.equals("REUSED")
        || result.equals("REVOKED")) {
      return Optional.empty();
    }
    return decode(result, familyId).map(stored -> new RefreshSession(stored.user(), nextToken));
  }

  @Override
  public void revoke(String token) {
    String familyId = familyId(token);
    if (familyId == null) return;
    String activeHash = redis.opsForValue().get(familyKey(familyId));
    if (activeHash != null) redis.delete(tokenKey(activeHash));
    redis.delete(familyKey(familyId));
    redis.opsForValue().set(revokedKey(familyId), "1", Duration.ofDays(30));
  }

  private String token(String familyId) {
    return familyId + "." + SecureTokens.random();
  }

  private String familyId(String token) {
    if (token == null) return null;
    int separator = token.indexOf('.');
    if (separator <= 0) return null;
    try {
      return UUID.fromString(token.substring(0, separator)).toString();
    } catch (IllegalArgumentException exception) {
      return null;
    }
  }

  private String encode(AuthenticatedUser user, String familyId) {
    return objectMapper.writeValueAsString(new StoredSession(user, familyId));
  }

  private Optional<StoredSession> decode(String value, String familyId) {
    try {
      StoredSession session = objectMapper.readValue(value, StoredSession.class);
      if (!familyId.equals(session.familyId())) throw new IllegalArgumentException();
      return Optional.of(session);
    } catch (RuntimeException exception) {
      return Optional.empty();
    }
  }

  private String tokenKey(String hash) {
    return TOKEN_PREFIX + hash;
  }

  private String spentKey(String hash) {
    return SPENT_PREFIX + hash;
  }

  private String familyKey(String familyId) {
    return FAMILY_PREFIX + familyId;
  }

  private String revokedKey(String familyId) {
    return REVOKED_PREFIX + familyId;
  }

  private record StoredSession(AuthenticatedUser user, String familyId) {}
}
