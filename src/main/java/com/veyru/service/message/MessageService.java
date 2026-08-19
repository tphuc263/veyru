package com.veyru.service.message;

import com.veyru.dto.response.message.ConversationResponse;
import com.veyru.dto.response.message.MessageResponse;
import com.veyru.dto.websocket.WsEventEnvelope;
import com.veyru.exceptions.ApiException;
import com.veyru.exceptions.ErrorCode;
import com.veyru.model.Conversation;
import com.veyru.model.Message;
import com.veyru.model.User;
import com.veyru.repository.ConversationRepository;
import com.veyru.repository.MessageRepository;
import com.veyru.repository.UserRepository;
import com.veyru.security.userdetails.AppUserDetails;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

  private final MessageRepository messageRepository;
  private final ConversationRepository conversationRepository;
  private final UserRepository userRepository;
  private final MongoTemplate mongoTemplate;
  private final StringRedisTemplate redisTemplate;
  private final SimpMessagingTemplate messagingTemplate;

  /** Send a message from current user to receiver */
  public MessageResponse sendMessage(
      String senderId, String receiverId, String text, String clientMessageId) {
    // Apply Idempotency with Redis
    if (clientMessageId != null && !clientMessageId.trim().isEmpty()) {
      String redisKey = "msg_idempotency:" + clientMessageId;
      Boolean isNew = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", 24, TimeUnit.HOURS);
      if (Boolean.FALSE.equals(isNew)) {
        log.info(
            "Duplicate message detected, skipping db save for clientMessageId: {}",
            clientMessageId);
        MessageResponse duplicateResponse = new MessageResponse();
        duplicateResponse.setSenderId(senderId);
        duplicateResponse.setReceiverId(receiverId);
        duplicateResponse.setText(text);
        duplicateResponse.setCreatedAt(Instant.now());
        return duplicateResponse;
      }
    }

    User sender =
        userRepository
            .findById(senderId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    User receiver =
        userRepository
            .findById(receiverId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));

    // Find or create conversation
    Conversation conversation = getOrCreateConversation(senderId, receiverId);

    // Create message
    Message message = new Message();
    message.setConversationId(conversation.getId());
    message.setSenderId(senderId);
    message.setReceiverId(receiverId);
    message.setText(text);
    message.setRead(false);
    message.setCreatedAt(Instant.now());
    message = messageRepository.save(message);

    // Update conversation with last message
    conversation.setLastMessageText(text);
    conversation.setLastMessageSenderId(senderId);
    conversation.setLastMessageAt(Instant.now());
    conversation.setUpdatedAt(Instant.now());
    conversationRepository.save(conversation);

    MessageResponse response = toMessageResponse(message);

    // Send via WebSocket
    try {
      WsEventEnvelope<MessageResponse> envelope =
          WsEventEnvelope.<MessageResponse>builder()
              .type("CHAT_MESSAGE")
              .clientMessageId(clientMessageId)
              .timestamp(Instant.now())
              .payload(response)
              .build();
      // Send to receiver
      messagingTemplate.convertAndSendToUser(receiverId, "/queue/messages", envelope);
      log.info("Sent real-time message to receiver: {}", receiverId);
      // Send back to sender (to replace optimistic message with confirmed data)
      messagingTemplate.convertAndSendToUser(senderId, "/queue/messages", envelope);
      log.info("Sent confirmed message back to sender: {}", senderId);
    } catch (Exception e) {
      log.error("Failed to send real-time message", e);
    }

    return response;
  }

  /** Get all conversations for the current user */
  public List<ConversationResponse> getConversations(String userId) {
    Sort sort = Sort.by(Sort.Direction.DESC, "lastMessageAt");
    List<Conversation> conversations =
        conversationRepository.findByParticipantIdsContaining(userId, sort);

    List<ConversationResponse> responses = new ArrayList<>();
    for (Conversation conv : conversations) {
      String otherUserId =
          conv.getParticipantIds().stream()
              .filter(id -> !id.equals(userId))
              .findFirst()
              .orElse(userId);

      User otherUser = userRepository.findById(otherUserId).orElse(null);
      if (otherUser == null) continue;

      long unreadCount =
          messageRepository.countByConversationIdAndReceiverIdAndReadFalse(conv.getId(), userId);

      ConversationResponse response = new ConversationResponse();
      response.setId(conv.getId());
      response.setParticipantId(otherUser.getId());
      response.setParticipantUsername(otherUser.getUsername());
      response.setParticipantImageUrl(otherUser.getImageUrl());
      response.setLastMessageText(conv.getLastMessageText());
      response.setLastMessageSenderId(conv.getLastMessageSenderId());
      response.setLastMessageAt(conv.getLastMessageAt());
      response.setUnreadCount(unreadCount);

      responses.add(response);
    }

    return responses;
  }

  /** Get messages for a specific conversation */
  public Page<MessageResponse> getMessages(
      String conversationId, String userId, int page, int size) {
    Conversation conversation =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));

    // Verify user is a participant
    if (!conversation.getParticipantIds().contains(userId)) {
      throw new ApiException(ErrorCode.ACCESS_DENIED);
    }

    Page<Message> messages =
        messageRepository.findByConversationIdOrderByCreatedAtDesc(
            conversationId, PageRequest.of(page, size));

    return messages.map(this::toMessageResponse);
  }

  /** Mark messages as read */
  public void markMessagesAsRead(String conversationId, String userId) {
    Query query =
        new Query(
            Criteria.where("conversationId")
                .is(conversationId)
                .and("receiverId")
                .is(userId)
                .and("read")
                .is(false));
    Update update = new Update().set("read", true);
    var result = mongoTemplate.updateMulti(query, update, Message.class);

    // Notify the sender that their messages have been read
    if (result.getModifiedCount() > 0) {
      Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
      if (conversation != null) {
        conversation.getParticipantIds().stream()
            .filter(id -> !id.equals(userId))
            .forEach(
                senderId -> {
                  try {
                    WsEventEnvelope<java.util.Map<String, String>> envelope =
                        WsEventEnvelope.<java.util.Map<String, String>>builder()
                            .type("MESSAGES_READ")
                            .timestamp(Instant.now())
                            .payload(
                                java.util.Map.of(
                                    "conversationId", conversationId,
                                    "readBy", userId))
                            .build();
                    messagingTemplate.convertAndSendToUser(senderId, "/queue/messages", envelope);
                    log.info("Sent MESSAGES_READ notification to user: {}", senderId);
                  } catch (Exception e) {
                    log.error("Failed to send MESSAGES_READ notification to user: {}", senderId, e);
                  }
                });
      }
    }
  }

  /** Get total unread count for a user */
  public long getUnreadCount(String userId) {
    return messageRepository.countByReceiverIdAndReadFalse(userId);
  }

  /**
   * Get or create a conversation between two users. Synchronized to prevent race conditions
   * creating duplicate conversations.
   */
  public synchronized Conversation getOrCreateConversation(String userId1, String userId2) {
    // Try to find existing conversation
    List<Conversation> convs =
        conversationRepository.findByParticipantIdsContaining(userId1, Sort.unsorted());

    for (Conversation conv : convs) {
      if (conv.getParticipantIds().contains(userId2)) {
        return conv;
      }
    }

    // Create new conversation
    Conversation conversation = new Conversation();
    conversation.setParticipantIds(List.of(userId1, userId2));
    conversation.setCreatedAt(Instant.now());
    conversation.setUpdatedAt(Instant.now());
    return conversationRepository.save(conversation);
  }

  /** Get the current authenticated user ID */
  public String getCurrentUserId() {
    AppUserDetails userDetails =
        (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return userDetails.getId();
  }

  private MessageResponse toMessageResponse(Message message) {
    MessageResponse response = new MessageResponse();
    response.setId(message.getId());
    response.setConversationId(message.getConversationId());
    response.setSenderId(message.getSenderId());
    response.setReceiverId(message.getReceiverId());
    response.setText(message.getText());
    response.setRead(message.isRead());
    response.setCreatedAt(message.getCreatedAt());
    return response;
  }
}
