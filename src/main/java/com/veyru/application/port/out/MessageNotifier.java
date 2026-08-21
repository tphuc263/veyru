package com.veyru.application.port.out;

import com.veyru.application.messaging.MessageResult;

public interface MessageNotifier {
  void messageSent(
      String senderId, String receiverId, String clientMessageId, MessageResult message);

  void messagesRead(String recipientId, String conversationId, String readBy);
}
