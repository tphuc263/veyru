package com.veyru.adapter.out.mongo;

import com.veyru.application.port.out.ConversationStore;
import com.veyru.domain.model.Conversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class MongoConversationStore implements ConversationStore {
  private final SpringDataConversationRepository repository;

  public MongoConversationStore(SpringDataConversationRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Conversation> findById(String conversationId) {
    return repository.findById(conversationId).map(ConversationDocument::toDomain);
  }

  @Override
  public Optional<Conversation> findBetween(String firstUserId, String secondUserId) {
    return repository.findByParticipantIdsContaining(firstUserId, Sort.unsorted()).stream()
        .map(ConversationDocument::toDomain)
        .filter(conversation -> conversation.hasParticipant(secondUserId))
        .findFirst();
  }

  @Override
  public List<Conversation> findByParticipantId(String userId) {
    return repository
        .findByParticipantIdsContaining(userId, Sort.by(Sort.Direction.DESC, "lastMessageAt"))
        .stream()
        .map(ConversationDocument::toDomain)
        .toList();
  }

  @Override
  public Conversation save(Conversation conversation) {
    return repository.save(ConversationDocument.from(conversation)).toDomain();
  }
}
