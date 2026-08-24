package com.veyru.adapter.out.redis;

import com.veyru.application.port.out.MessageIdempotency;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisMessageIdempotency implements MessageIdempotency {
  private static final long TTL_HOURS = 24;
  private final StringRedisTemplate redisTemplate;

  public RedisMessageIdempotency(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public boolean claim(String senderId, String clientMessageId) {
    return Boolean.TRUE.equals(
        redisTemplate
            .opsForValue()
            .setIfAbsent(
                "msg_idempotency:" + senderId + ":" + clientMessageId,
                "1",
                Duration.ofHours(TTL_HOURS)));
  }
}
