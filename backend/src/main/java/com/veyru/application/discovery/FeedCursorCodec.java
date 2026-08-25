package com.veyru.application.discovery;

import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.config.NewsfeedProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;

public class FeedCursorCodec {
  private static final String VERSION = "v1";
  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
  private final byte[] secret;
  private final Clock clock;
  private final NewsfeedProperties properties;

  public String encode(String viewerId, FeedCursor cursor) {
    String payload =
        String.join(
            "\n",
            VERSION,
            viewerId,
            Long.toString(cursor.rankedAt().toEpochMilli()),
            Double.toHexString(cursor.lastScore()),
            Long.toString(cursor.lastCreatedAt().toEpochMilli()),
            cursor.lastReference());
    byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
    return ENCODER.encodeToString(bytes) + "." + ENCODER.encodeToString(sign(bytes));
  }

  public FeedCursor decode(String viewerId, String value) {
    try {
      String[] token = value.split("\\.", -1);
      if (token.length != 2) throw invalid();
      byte[] payload = DECODER.decode(token[0]);
      if (!MessageDigest.isEqual(sign(payload), DECODER.decode(token[1]))) throw invalid();
      String[] fields = new String(payload, StandardCharsets.UTF_8).split("\n", -1);
      if (fields.length != 6 || !VERSION.equals(fields[0]) || !viewerId.equals(fields[1])) {
        throw invalid();
      }
      Instant rankedAt = Instant.ofEpochMilli(Long.parseLong(fields[2]));
      Instant now = clock.instant();
      if (rankedAt.isAfter(now) || rankedAt.plus(properties.cache().cursorTtl()).isBefore(now)) {
        throw invalid();
      }
      return new FeedCursor(
          rankedAt,
          Double.valueOf(fields[3]),
          Instant.ofEpochMilli(Long.parseLong(fields[4])),
          fields[5]);
    } catch (UseCaseException exception) {
      throw exception;
    } catch (RuntimeException exception) {
      throw invalid();
    }
  }

  private byte[] sign(byte[] payload) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return mac.doFinal(payload);
    } catch (Exception exception) {
      throw new IllegalStateException("HmacSHA256 is required by the Java platform", exception);
    }
  }

  private UseCaseException invalid() {
    return new UseCaseException(UseCaseError.INVALID_REQUEST_VALUE);
  }

  public FeedCursorCodec(
      @Value("${auth.token.jwtSecret}") String secret, Clock clock, NewsfeedProperties properties) {
    this.secret = ("veyru-feed-cursor:" + secret).getBytes(StandardCharsets.UTF_8);
    this.clock = clock;
    this.properties = properties;
  }

  public record FeedCursor(
      Instant rankedAt, double lastScore, Instant lastCreatedAt, String lastReference) {}
}
