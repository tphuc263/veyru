package com.veyru.adapter.out.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import com.veyru.domain.enums.NotificationType;
import com.veyru.domain.model.Comment;
import com.veyru.domain.model.CommentLike;
import com.veyru.domain.model.Conversation;
import com.veyru.domain.model.Favorite;
import com.veyru.domain.model.Follow;
import com.veyru.domain.model.Like;
import com.veyru.domain.model.Message;
import com.veyru.domain.model.Notification;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.Share;
import com.veyru.domain.model.User;
import com.veyru.domain.model.UserSnapshot;
import com.veyru.support.DomainFixtures;
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
  private static final Instant NOW = Instant.parse("2026-08-23T00:00:00Z");
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
  void roundTripsEveryDomainDocument() {
    User user = DomainFixtures.user("user");
    Photo photo = DomainFixtures.photo("photo", "user", NOW);
    Comment comment =
        Comment.restore(
            "comment",
            "photo",
            "text",
            NOW,
            new UserSnapshot("user", "alice"),
            null,
            0,
            0,
            List.of());
    Notification notification =
        Notification.restore(
            "notification",
            "user",
            NotificationType.NEW_FOLLOWER,
            null,
            null,
            "message",
            false,
            NOW,
            new UserSnapshot("other", "bob"),
            null);
    Follow follow = new Follow("follow", "user", "other", NOW);
    Like like = new Like("like", "photo", "user", NOW);
    CommentLike commentLike = new CommentLike("comment-like", "comment", "user", NOW);
    Favorite favorite = new Favorite("favorite", "user", "photo", NOW);
    Share share = new Share("share", "photo", "user", "caption", NOW);
    Conversation conversation =
        new Conversation("conversation", List.of("user", "other"), null, null, null, NOW, NOW);
    Message message = new Message("message", "conversation", "user", "other", "hello", false, NOW);

    assertThat(roundTrip(UserDocument.fromDomain(user), UserDocument.class).toDomain().username())
        .isEqualTo("user");
    assertThat(roundTrip(PhotoDocument.fromDomain(photo), PhotoDocument.class).toDomain().id())
        .isEqualTo("photo");
    assertThat(
            roundTrip(CommentDocument.fromDomain(comment), CommentDocument.class).toDomain().id())
        .isEqualTo("comment");
    assertThat(
            roundTrip(NotificationDocument.fromDomain(notification), NotificationDocument.class)
                .toDomain()
                .id())
        .isEqualTo("notification");
    assertThat(roundTrip(FollowDocument.fromDomain(follow), FollowDocument.class).toDomain())
        .isEqualTo(follow);
    assertThat(roundTrip(LikeDocument.fromDomain(like), LikeDocument.class).toDomain())
        .isEqualTo(like);
    assertThat(
            roundTrip(CommentLikeDocument.fromDomain(commentLike), CommentLikeDocument.class)
                .toDomain())
        .isEqualTo(commentLike);
    assertThat(roundTrip(FavoriteDocument.fromDomain(favorite), FavoriteDocument.class).toDomain())
        .isEqualTo(favorite);
    assertThat(roundTrip(ShareDocument.fromDomain(share), ShareDocument.class).toDomain())
        .isEqualTo(share);
    assertThat(
            roundTrip(ConversationDocument.fromDomain(conversation), ConversationDocument.class)
                .toDomain())
        .isEqualTo(conversation);
    assertThat(roundTrip(MessageDocument.fromDomain(message), MessageDocument.class).toDomain())
        .isEqualTo(message);
  }

  @Test
  void preservesMongoQueryPathsForPhotoSnapshots() {
    Document document = new Document();
    converter.write(
        PhotoDocument.fromDomain(DomainFixtures.photo("photo", "author", NOW)), document);

    assertThat(document.getEmbedded(List.of("user", "userId"), String.class)).isEqualTo("author");
    assertThat(document).containsKeys("userTags", "likeCount", "commentCount", "shareCount");
  }

  private <T> T roundTrip(T value, Class<T> type) {
    Document document = new Document();
    converter.write(value, document);
    return converter.read(type, document);
  }
}
