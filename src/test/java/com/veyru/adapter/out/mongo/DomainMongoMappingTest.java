package com.veyru.adapter.out.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import com.veyru.domain.enums.NotificationType;
import com.veyru.domain.enums.UserRole;
import com.veyru.domain.model.Comment;
import com.veyru.domain.model.Follow;
import com.veyru.domain.model.Notification;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.time.Instant;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

class DomainMongoMappingTest {
  private MappingMongoConverter converter;

  @BeforeEach
  void setUp() throws Exception {
    MongoCustomConversions conversions =
        MongoCustomConversions.create(adapter -> adapter.useNativeDriverJavaTimeCodecs());
    MongoMappingContext context = new MongoMappingContext();
    context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
    context.afterPropertiesSet();
    converter = new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, context);
    converter.setCustomConversions(conversions);
    converter.afterPropertiesSet();
  }

  @Test
  void roundTripsHardenedAggregatesWithoutPublicSetters() {
    Instant now = Instant.parse("2026-08-23T00:00:00Z");
    User user =
        new User(
            "user",
            "alice",
            "alice@example.com",
            null,
            "hash",
            UserRole.ROLE_USER,
            null,
            null,
            now,
            0,
            0,
            0,
            null,
            null);
    Photo photo =
        new Photo(
            "photo",
            "image",
            "caption",
            now,
            List.of("tag"),
            new Photo.EmbeddedUser("user", "alice"),
            0,
            0,
            0,
            List.of());
    Follow follow = new Follow("follow", "user", "other", now);
    Comment comment =
        new Comment(
            "comment",
            "photo",
            "user",
            "text",
            now,
            new Comment.EmbeddedUser("user", "alice"),
            null,
            0,
            0,
            List.of());
    Notification notification =
        new Notification(
            "notification",
            "user",
            "other",
            NotificationType.NEW_FOLLOWER,
            null,
            null,
            "message",
            false,
            now,
            new Notification.EmbeddedActor("bob"),
            null);

    assertThat(roundTrip(user, User.class).getUsername()).isEqualTo("alice");
    assertThat(roundTrip(photo, Photo.class).getImageUrl()).isEqualTo("image");
    assertThat(roundTrip(follow, Follow.class).getFollowingId()).isEqualTo("other");
    assertThat(roundTrip(comment, Comment.class).getText()).isEqualTo("text");
    assertThat(roundTrip(notification, Notification.class).getMessage()).isEqualTo("message");
  }

  private <T> T roundTrip(T value, Class<T> type) {
    Document document = new Document();
    converter.write(value, document);
    return converter.read(type, document);
  }
}
