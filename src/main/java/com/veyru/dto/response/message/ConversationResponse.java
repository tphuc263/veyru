package com.veyru.dto.response.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public class ConversationResponse {
  private String id;
  private String participantId;
  private String participantUsername;
  private String participantImageUrl;
  private String lastMessageText;
  private String lastMessageSenderId;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant lastMessageAt;

  private long unreadCount;

  public String getId() {
    return this.id;
  }

  public String getParticipantId() {
    return this.participantId;
  }

  public String getParticipantUsername() {
    return this.participantUsername;
  }

  public String getParticipantImageUrl() {
    return this.participantImageUrl;
  }

  public String getLastMessageText() {
    return this.lastMessageText;
  }

  public String getLastMessageSenderId() {
    return this.lastMessageSenderId;
  }

  public Instant getLastMessageAt() {
    return this.lastMessageAt;
  }

  public long getUnreadCount() {
    return this.unreadCount;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setParticipantId(final String participantId) {
    this.participantId = participantId;
  }

  public void setParticipantUsername(final String participantUsername) {
    this.participantUsername = participantUsername;
  }

  public void setParticipantImageUrl(final String participantImageUrl) {
    this.participantImageUrl = participantImageUrl;
  }

  public void setLastMessageText(final String lastMessageText) {
    this.lastMessageText = lastMessageText;
  }

  public void setLastMessageSenderId(final String lastMessageSenderId) {
    this.lastMessageSenderId = lastMessageSenderId;
  }

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  public void setLastMessageAt(final Instant lastMessageAt) {
    this.lastMessageAt = lastMessageAt;
  }

  public void setUnreadCount(final long unreadCount) {
    this.unreadCount = unreadCount;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof ConversationResponse)) return false;
    final ConversationResponse other = (ConversationResponse) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    if (this.getUnreadCount() != other.getUnreadCount()) return false;
    final java.lang.Object this$id = this.getId();
    final java.lang.Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final java.lang.Object this$participantId = this.getParticipantId();
    final java.lang.Object other$participantId = other.getParticipantId();
    if (this$participantId == null
        ? other$participantId != null
        : !this$participantId.equals(other$participantId)) return false;
    final java.lang.Object this$participantUsername = this.getParticipantUsername();
    final java.lang.Object other$participantUsername = other.getParticipantUsername();
    if (this$participantUsername == null
        ? other$participantUsername != null
        : !this$participantUsername.equals(other$participantUsername)) return false;
    final java.lang.Object this$participantImageUrl = this.getParticipantImageUrl();
    final java.lang.Object other$participantImageUrl = other.getParticipantImageUrl();
    if (this$participantImageUrl == null
        ? other$participantImageUrl != null
        : !this$participantImageUrl.equals(other$participantImageUrl)) return false;
    final java.lang.Object this$lastMessageText = this.getLastMessageText();
    final java.lang.Object other$lastMessageText = other.getLastMessageText();
    if (this$lastMessageText == null
        ? other$lastMessageText != null
        : !this$lastMessageText.equals(other$lastMessageText)) return false;
    final java.lang.Object this$lastMessageSenderId = this.getLastMessageSenderId();
    final java.lang.Object other$lastMessageSenderId = other.getLastMessageSenderId();
    if (this$lastMessageSenderId == null
        ? other$lastMessageSenderId != null
        : !this$lastMessageSenderId.equals(other$lastMessageSenderId)) return false;
    final java.lang.Object this$lastMessageAt = this.getLastMessageAt();
    final java.lang.Object other$lastMessageAt = other.getLastMessageAt();
    if (this$lastMessageAt == null
        ? other$lastMessageAt != null
        : !this$lastMessageAt.equals(other$lastMessageAt)) return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof ConversationResponse;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final long $unreadCount = this.getUnreadCount();
    result = result * PRIME + (int) ($unreadCount >>> 32 ^ $unreadCount);
    final java.lang.Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final java.lang.Object $participantId = this.getParticipantId();
    result = result * PRIME + ($participantId == null ? 43 : $participantId.hashCode());
    final java.lang.Object $participantUsername = this.getParticipantUsername();
    result = result * PRIME + ($participantUsername == null ? 43 : $participantUsername.hashCode());
    final java.lang.Object $participantImageUrl = this.getParticipantImageUrl();
    result = result * PRIME + ($participantImageUrl == null ? 43 : $participantImageUrl.hashCode());
    final java.lang.Object $lastMessageText = this.getLastMessageText();
    result = result * PRIME + ($lastMessageText == null ? 43 : $lastMessageText.hashCode());
    final java.lang.Object $lastMessageSenderId = this.getLastMessageSenderId();
    result = result * PRIME + ($lastMessageSenderId == null ? 43 : $lastMessageSenderId.hashCode());
    final java.lang.Object $lastMessageAt = this.getLastMessageAt();
    result = result * PRIME + ($lastMessageAt == null ? 43 : $lastMessageAt.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "ConversationResponse(id="
        + this.getId()
        + ", participantId="
        + this.getParticipantId()
        + ", participantUsername="
        + this.getParticipantUsername()
        + ", participantImageUrl="
        + this.getParticipantImageUrl()
        + ", lastMessageText="
        + this.getLastMessageText()
        + ", lastMessageSenderId="
        + this.getLastMessageSenderId()
        + ", lastMessageAt="
        + this.getLastMessageAt()
        + ", unreadCount="
        + this.getUnreadCount()
        + ")";
  }

  public ConversationResponse() {}

  public ConversationResponse(
      final String id,
      final String participantId,
      final String participantUsername,
      final String participantImageUrl,
      final String lastMessageText,
      final String lastMessageSenderId,
      final Instant lastMessageAt,
      final long unreadCount) {
    this.id = id;
    this.participantId = participantId;
    this.participantUsername = participantUsername;
    this.participantImageUrl = participantImageUrl;
    this.lastMessageText = lastMessageText;
    this.lastMessageSenderId = lastMessageSenderId;
    this.lastMessageAt = lastMessageAt;
    this.unreadCount = unreadCount;
  }
}
