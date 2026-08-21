package com.veyru.adapter.out.mongo;

import com.veyru.domain.model.Conversation;
import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "conversations")
@CompoundIndex(name = "participants_idx", def = "{\'participantIds\': 1}")
public class ConversationDocument {
  @Id private String id;
  private List<String> participantIds;
  private String lastMessageText;
  private String lastMessageSenderId;
  private Instant lastMessageAt;
  private Instant createdAt;
  private Instant updatedAt;

  public Conversation toDomain() {
    return new Conversation(
        id,
        participantIds,
        lastMessageText,
        lastMessageSenderId,
        lastMessageAt,
        createdAt,
        updatedAt);
  }

  public static ConversationDocument from(Conversation conversation) {
    ConversationDocument document = new ConversationDocument();
    document.id = conversation.id();
    document.participantIds = conversation.participantIds();
    document.lastMessageText = conversation.lastMessageText();
    document.lastMessageSenderId = conversation.lastMessageSenderId();
    document.lastMessageAt = conversation.lastMessageAt();
    document.createdAt = conversation.createdAt();
    document.updatedAt = conversation.updatedAt();
    return document;
  }
}
