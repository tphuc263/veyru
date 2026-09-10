package com.veyru.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.veyru.config.RedisConfig;
import io.lettuce.core.RedisCommandExecutionException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
class RedisVectorAdapterStartupTest {
  @Container
  static final GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine")).withExposedPorts(6379);

  static LettuceConnectionFactory connectionFactory;
  static RedisTemplate<String, Object> template;

  @BeforeAll
  static void connect() {
    connectionFactory = new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
    connectionFactory.afterPropertiesSet();
    template = new RedisConfig().redisTemplate(connectionFactory, new ObjectMapper());
  }

  @AfterAll
  static void disconnect() {
    if (connectionFactory != null) connectionFactory.destroy();
  }

  @Test
  void applicationFailsStartupWhenRedisSearchIsUnavailable() {
    RedisVectorAdapter adapter = new RedisVectorAdapter(template);

    assertThatThrownBy(adapter::initializeIndexes).isInstanceOf(RedisSystemException.class);
  }

  @Test
  @SuppressWarnings("unchecked")
  void ignoresSearchIndexExistsErrorWhenIndexAlreadyExists() {
    RedisTemplate<String, Object> mockTemplate = mock(RedisTemplate.class);
    RedisCommandExecutionException cause =
        new RedisCommandExecutionException("SEARCH_INDEX_EXISTS Index already exists");
    when(mockTemplate.execute(any(RedisCallback.class)))
        .thenThrow(new RedisSystemException("Error in execution", cause));

    RedisVectorAdapter adapter = new RedisVectorAdapter(mockTemplate);
    assertThatCode(adapter::initializeIndexes).doesNotThrowAnyException();
  }
}
