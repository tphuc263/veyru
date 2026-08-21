package com.veyru.adapter.out.mongo;

import com.veyru.domain.model.Message;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "messages")
@CompoundIndex(
    name = "conversation_created_idx",
    def = "{\'conversationId\': 1, \'createdAt\': -1}")
public class MessageDocument {
  @Id private String id;
  private String conversationId;
  private String senderId;
  private String receiverId;
  private String text;
  private boolean read;
  private Instant createdAt;

  public Message toDomain() {
    return new Message(id, conversationId, senderId, receiverId, text, read, createdAt);
  }

  public static MessageDocument from(Message message) {
    MessageDocument document = new MessageDocument();
    document.id = message.id();
    document.conversationId = message.conversationId();
    document.senderId = message.senderId();
    document.receiverId = message.receiverId();
    document.text = message.text();
    document.read = message.read();
    document.createdAt = message.createdAt();
    return document;
  }
}
