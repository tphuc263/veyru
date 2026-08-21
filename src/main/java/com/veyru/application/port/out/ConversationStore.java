package com.veyru.application.port.out;

import com.veyru.domain.model.Conversation;
import java.util.List;
import java.util.Optional;

public interface ConversationStore {
  Optional<Conversation> findById(String conversationId);

  Optional<Conversation> findBetween(String firstUserId, String secondUserId);

  List<Conversation> findByParticipantId(String userId);

  Conversation save(Conversation conversation);
}
