package com.veyru.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "messages")
@CompoundIndex(
    name = "conversation_created_idx",
    def = "{\'conversationId\': 1, \'createdAt\': -1}")
public class Message {
  @Id private String id;
  private String conversationId;
  private String senderId;
  private String receiverId;
  private String text;
  private boolean read;
  private Instant createdAt;

  public String getId() {
    return this.id;
  }

  public String getConversationId() {
    return this.conversationId;
  }

  public String getSenderId() {
    return this.senderId;
  }

  public String getReceiverId() {
    return this.receiverId;
  }

  public String getText() {
    return this.text;
  }

  public boolean isRead() {
    return this.read;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setConversationId(final String conversationId) {
    this.conversationId = conversationId;
  }

  public void setSenderId(final String senderId) {
    this.senderId = senderId;
  }

  public void setReceiverId(final String receiverId) {
    this.receiverId = receiverId;
  }

  public void setText(final String text) {
    this.text = text;
  }

  public void setRead(final boolean read) {
    this.read = read;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof Message)) return false;
    final Message other = (Message) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    if (this.isRead() != other.isRead()) return false;
    final java.lang.Object this$id = this.getId();
    final java.lang.Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final java.lang.Object this$conversationId = this.getConversationId();
    final java.lang.Object other$conversationId = other.getConversationId();
    if (this$conversationId == null
        ? other$conversationId != null
        : !this$conversationId.equals(other$conversationId)) return false;
    final java.lang.Object this$senderId = this.getSenderId();
    final java.lang.Object other$senderId = other.getSenderId();
    if (this$senderId == null ? other$senderId != null : !this$senderId.equals(other$senderId))
      return false;
    final java.lang.Object this$receiverId = this.getReceiverId();
    final java.lang.Object other$receiverId = other.getReceiverId();
    if (this$receiverId == null
        ? other$receiverId != null
        : !this$receiverId.equals(other$receiverId)) return false;
    final java.lang.Object this$text = this.getText();
    final java.lang.Object other$text = other.getText();
    if (this$text == null ? other$text != null : !this$text.equals(other$text)) return false;
    final java.lang.Object this$createdAt = this.getCreatedAt();
    final java.lang.Object other$createdAt = other.getCreatedAt();
    if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt))
      return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof Message;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + (this.isRead() ? 79 : 97);
    final java.lang.Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final java.lang.Object $conversationId = this.getConversationId();
    result = result * PRIME + ($conversationId == null ? 43 : $conversationId.hashCode());
    final java.lang.Object $senderId = this.getSenderId();
    result = result * PRIME + ($senderId == null ? 43 : $senderId.hashCode());
    final java.lang.Object $receiverId = this.getReceiverId();
    result = result * PRIME + ($receiverId == null ? 43 : $receiverId.hashCode());
    final java.lang.Object $text = this.getText();
    result = result * PRIME + ($text == null ? 43 : $text.hashCode());
    final java.lang.Object $createdAt = this.getCreatedAt();
    result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "Message(id="
        + this.getId()
        + ", conversationId="
        + this.getConversationId()
        + ", senderId="
        + this.getSenderId()
        + ", receiverId="
        + this.getReceiverId()
        + ", text="
        + this.getText()
        + ", read="
        + this.isRead()
        + ", createdAt="
        + this.getCreatedAt()
        + ")";
  }

  public Message() {}

  public Message(
      final String id,
      final String conversationId,
      final String senderId,
      final String receiverId,
      final String text,
      final boolean read,
      final Instant createdAt) {
    this.id = id;
    this.conversationId = conversationId;
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.text = text;
    this.read = read;
    this.createdAt = createdAt;
  }
}
