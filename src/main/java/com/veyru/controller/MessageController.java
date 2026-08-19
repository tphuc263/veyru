package com.veyru.controller;

import com.veyru.dto.request.message.SendMessageRequest;
import com.veyru.dto.response.PageResponse;
import com.veyru.dto.response.message.ConversationResponse;
import com.veyru.dto.response.message.MessageResponse;
import com.veyru.service.message.MessageService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

  private final MessageService messageService;
  private final SimpUserRegistry simpUserRegistry;

  /** Get all conversations for the current user */
  @GetMapping("/conversations")
  public ResponseEntity<List<ConversationResponse>> getConversations() {
    String userId = messageService.getCurrentUserId();
    List<ConversationResponse> conversations = messageService.getConversations(userId);
    return ResponseEntity.ok(conversations);
  }

  /** Get messages for a specific conversation */
  @GetMapping("/conversations/{conversationId}")
  public ResponseEntity<PageResponse<MessageResponse>> getMessages(
      @PathVariable String conversationId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {
    String userId = messageService.getCurrentUserId();
    Page<MessageResponse> messages = messageService.getMessages(conversationId, userId, page, size);
    return ResponseEntity.ok(PageResponse.from(messages));
  }

  /** Send a message via REST */
  @PostMapping("/send")
  public ResponseEntity<MessageResponse> sendMessage(@RequestBody SendMessageRequest request) {
    String senderId = messageService.getCurrentUserId();
    MessageResponse message =
        messageService.sendMessage(senderId, request.getReceiverId(), request.getText(), null);
    return ResponseEntity.status(201).body(message);
  }

  /** Mark messages as read */
  @PostMapping("/conversations/{conversationId}/read")
  public ResponseEntity<Void> markAsRead(@PathVariable String conversationId) {
    String userId = messageService.getCurrentUserId();
    messageService.markMessagesAsRead(conversationId, userId);
    return ResponseEntity.noContent().build();
  }

  /** Get unread message count */
  @GetMapping("/unread-count")
  public ResponseEntity<Long> getUnreadCount() {
    String userId = messageService.getCurrentUserId();
    long count = messageService.getUnreadCount(userId);
    return ResponseEntity.ok(count);
  }

  /** Check if a user is online */
  @GetMapping("/online/{userId}")
  public ResponseEntity<Boolean> isUserOnline(@PathVariable String userId) {
    boolean online = simpUserRegistry.getUser(userId) != null;
    return ResponseEntity.ok(online);
  }

  /** Check online status for multiple users */
  @PostMapping("/online-users")
  public ResponseEntity<Map<String, Boolean>> getOnlineUsers(@RequestBody List<String> userIds) {
    Map<String, Boolean> result =
        userIds.stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    id -> id, id -> simpUserRegistry.getUser(id) != null));
    return ResponseEntity.ok(result);
  }

  /** Start a conversation with a user (or get existing one) */
  @PostMapping("/conversations/start/{otherUserId}")
  public ResponseEntity<Map<String, String>> startConversation(@PathVariable String otherUserId) {
    String userId = messageService.getCurrentUserId();
    var conversation = messageService.getOrCreateConversation(userId, otherUserId);
    return ResponseEntity.ok(Map.of("conversationId", conversation.getId()));
  }
}
