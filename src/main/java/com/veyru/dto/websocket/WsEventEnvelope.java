package com.veyru.dto.websocket;

import java.time.Instant;

public class WsEventEnvelope<T> {
  private String type;
  private String clientMessageId;
  private Instant timestamp;
  private T payload;

  public static class WsEventEnvelopeBuilder<T> {
    private String type;
    private String clientMessageId;
    private Instant timestamp;
    private T payload;

    WsEventEnvelopeBuilder() {}

    /**
     * @return {@code this}.
     */
    public WsEventEnvelope.WsEventEnvelopeBuilder<T> type(final String type) {
      this.type = type;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public WsEventEnvelope.WsEventEnvelopeBuilder<T> clientMessageId(final String clientMessageId) {
      this.clientMessageId = clientMessageId;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public WsEventEnvelope.WsEventEnvelopeBuilder<T> timestamp(final Instant timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    /**
     * @return {@code this}.
     */
    public WsEventEnvelope.WsEventEnvelopeBuilder<T> payload(final T payload) {
      this.payload = payload;
      return this;
    }

    public WsEventEnvelope<T> build() {
      return new WsEventEnvelope<T>(this.type, this.clientMessageId, this.timestamp, this.payload);
    }

    @java.lang.Override
    public java.lang.String toString() {
      return "WsEventEnvelope.WsEventEnvelopeBuilder(type="
          + this.type
          + ", clientMessageId="
          + this.clientMessageId
          + ", timestamp="
          + this.timestamp
          + ", payload="
          + this.payload
          + ")";
    }
  }

  public static <T> WsEventEnvelope.WsEventEnvelopeBuilder<T> builder() {
    return new WsEventEnvelope.WsEventEnvelopeBuilder<T>();
  }

  public String getType() {
    return this.type;
  }

  public String getClientMessageId() {
    return this.clientMessageId;
  }

  public Instant getTimestamp() {
    return this.timestamp;
  }

  public T getPayload() {
    return this.payload;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public void setClientMessageId(final String clientMessageId) {
    this.clientMessageId = clientMessageId;
  }

  public void setTimestamp(final Instant timestamp) {
    this.timestamp = timestamp;
  }

  public void setPayload(final T payload) {
    this.payload = payload;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof WsEventEnvelope)) return false;
    final WsEventEnvelope<?> other = (WsEventEnvelope<?>) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    final java.lang.Object this$type = this.getType();
    final java.lang.Object other$type = other.getType();
    if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
    final java.lang.Object this$clientMessageId = this.getClientMessageId();
    final java.lang.Object other$clientMessageId = other.getClientMessageId();
    if (this$clientMessageId == null
        ? other$clientMessageId != null
        : !this$clientMessageId.equals(other$clientMessageId)) return false;
    final java.lang.Object this$timestamp = this.getTimestamp();
    final java.lang.Object other$timestamp = other.getTimestamp();
    if (this$timestamp == null ? other$timestamp != null : !this$timestamp.equals(other$timestamp))
      return false;
    final java.lang.Object this$payload = this.getPayload();
    final java.lang.Object other$payload = other.getPayload();
    if (this$payload == null ? other$payload != null : !this$payload.equals(other$payload))
      return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof WsEventEnvelope;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final java.lang.Object $type = this.getType();
    result = result * PRIME + ($type == null ? 43 : $type.hashCode());
    final java.lang.Object $clientMessageId = this.getClientMessageId();
    result = result * PRIME + ($clientMessageId == null ? 43 : $clientMessageId.hashCode());
    final java.lang.Object $timestamp = this.getTimestamp();
    result = result * PRIME + ($timestamp == null ? 43 : $timestamp.hashCode());
    final java.lang.Object $payload = this.getPayload();
    result = result * PRIME + ($payload == null ? 43 : $payload.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "WsEventEnvelope(type="
        + this.getType()
        + ", clientMessageId="
        + this.getClientMessageId()
        + ", timestamp="
        + this.getTimestamp()
        + ", payload="
        + this.getPayload()
        + ")";
  }

  public WsEventEnvelope() {}

  public WsEventEnvelope(
      final String type, final String clientMessageId, final Instant timestamp, final T payload) {
    this.type = type;
    this.clientMessageId = clientMessageId;
    this.timestamp = timestamp;
    this.payload = payload;
  }
}
