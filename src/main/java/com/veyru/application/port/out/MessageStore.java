package com.veyru.application.port.out;

import com.veyru.application.messaging.PageResult;
import com.veyru.domain.model.Message;

public interface MessageStore {
  Message save(Message message);

  PageResult<Message> findByConversationId(String conversationId, int page, int size);

  long countUnread(String conversationId, String receiverId);

  long countUnread(String receiverId);

  long markUnreadAsRead(String conversationId, String receiverId);
}
