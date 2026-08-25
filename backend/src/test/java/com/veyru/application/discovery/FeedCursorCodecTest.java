package com.veyru.application.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.discovery.FeedCursorCodec.FeedCursor;
import com.veyru.config.NewsfeedProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class FeedCursorCodecTest {
  private static final Instant NOW = Instant.parse("2026-08-25T00:00:00Z");
  private final NewsfeedProperties properties =
      new NewsfeedProperties(
          new NewsfeedProperties.Ranking(200, 30, .5, .4, .35, .25, .6, .3, .1, 72, 50),
          new NewsfeedProperties.Cache(Duration.ofMinutes(5), Duration.ofMinutes(5)));
  private final FeedCursorCodec codec = codecAt(NOW);

  @Test
  void roundTripsSignedSeekTuple() {
    FeedCursor expected = new FeedCursor(NOW, .75, NOW.minusSeconds(30), "PHOTO:42");

    assertThat(codec.decode("viewer", codec.encode("viewer", expected))).isEqualTo(expected);
  }

  @Test
  void rejectsTamperingWrongViewerAndExpiry() {
    String cursor = codec.encode("viewer", new FeedCursor(NOW, .75, NOW, "PHOTO:42"));

    String tampered = cursor.substring(0, cursor.length() - 1) + (cursor.endsWith("A") ? "B" : "A");
    assertInvalid(tampered, codec);
    assertInvalid(
        cursor,
        new FeedCursorCodec(
            "secret", Clock.fixed(NOW.plusSeconds(301), ZoneOffset.UTC), properties));
    assertThatThrownBy(() -> codec.decode("someone-else", cursor))
        .isInstanceOfSatisfying(
            UseCaseException.class,
            exception ->
                assertThat(exception.code()).isEqualTo(UseCaseError.INVALID_REQUEST_VALUE));
  }

  private void assertInvalid(String cursor, FeedCursorCodec decoder) {
    assertThatThrownBy(() -> decoder.decode("viewer", cursor))
        .isInstanceOfSatisfying(
            UseCaseException.class,
            exception ->
                assertThat(exception.code()).isEqualTo(UseCaseError.INVALID_REQUEST_VALUE));
  }

  private FeedCursorCodec codecAt(Instant instant) {
    return new FeedCursorCodec("secret", Clock.fixed(instant, ZoneOffset.UTC), properties);
  }
}
