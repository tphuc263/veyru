package com.veyru.model;

import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "conversations")
@CompoundIndex(name = "participants_idx", def = "{\'participantIds\': 1}")
public class Conversation {
  @Id private String id;
  private List<String> participantIds;
  private String lastMessageText;
  private String lastMessageSenderId;
  private Instant lastMessageAt;
  private Instant createdAt;
  private Instant updatedAt;

  public String getId() {
    return this.id;
  }

  public List<String> getParticipantIds() {
    return this.participantIds;
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

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public Instant getUpdatedAt() {
    return this.updatedAt;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setParticipantIds(final List<String> participantIds) {
    this.participantIds = participantIds;
  }

  public void setLastMessageText(final String lastMessageText) {
    this.lastMessageText = lastMessageText;
  }

  public void setLastMessageSenderId(final String lastMessageSenderId) {
    this.lastMessageSenderId = lastMessageSenderId;
  }

  public void setLastMessageAt(final Instant lastMessageAt) {
    this.lastMessageAt = lastMessageAt;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(final Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof Conversation)) return false;
    final Conversation other = (Conversation) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    final java.lang.Object this$id = this.getId();
    final java.lang.Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final java.lang.Object this$participantIds = this.getParticipantIds();
    final java.lang.Object other$participantIds = other.getParticipantIds();
    if (this$participantIds == null
        ? other$participantIds != null
        : !this$participantIds.equals(other$participantIds)) return false;
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
    final java.lang.Object this$createdAt = this.getCreatedAt();
    final java.lang.Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
      return false;
    final java.lang.Object this$updatedAt = this.getUpdatedAt();
    final java.lang.Object other$updatedAt = other.getUpdatedAt();
    if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt))
      return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof Conversation;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final java.lang.Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final java.lang.Object $participantIds = this.getParticipantIds();
    result = result * PRIME + ($participantIds == null ? 43 : $participantIds.hashCode());
    final java.lang.Object $lastMessageText = this.getLastMessageText();
    result = result * PRIME + ($lastMessageText == null ? 43 : $lastMessageText.hashCode());
    final java.lang.Object $lastMessageSenderId = this.getLastMessageSenderId();
    result = result * PRIME + ($lastMessageSenderId == null ? 43 : $lastMessageSenderId.hashCode());
    final java.lang.Object $lastMessageAt = this.getLastMessageAt();
    result = result * PRIME + ($lastMessageAt == null ? 43 : $lastMessageAt.hashCode());
    final java.lang.Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    final java.lang.Object $updatedAt = this.getUpdatedAt();
    result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "Conversation(id="
        + this.getId()
        + ", participantIds="
        + this.getParticipantIds()
        + ", lastMessageText="
        + this.getLastMessageText()
        + ", lastMessageSenderId="
        + this.getLastMessageSenderId()
        + ", lastMessageAt="
        + this.getLastMessageAt()
        + ", createdAt="
        + this.getCreatedAt()
        + ", updatedAt="
        + this.getUpdatedAt()
        + ")";
  }

  public Conversation() {}

  public Conversation(
      final String id,
      final List<String> participantIds,
      final String lastMessageText,
      final String lastMessageSenderId,
      final Instant lastMessageAt,
      final Instant createdAt,
      final Instant updatedAt) {
    this.id = id;
    this.participantIds = participantIds;
    this.lastMessageText = lastMessageText;
    this.lastMessageSenderId = lastMessageSenderId;
    this.lastMessageAt = lastMessageAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }
}
