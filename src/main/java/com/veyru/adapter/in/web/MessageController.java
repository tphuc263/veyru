package com.veyru.adapter.in.web;

import com.veyru.adapter.in.dto.SendMessageRequest;
import com.veyru.adapter.in.dto.response.PageResponse;
import com.veyru.adapter.in.security.userdetails.AppUserDetails;
import com.veyru.application.messaging.ConversationResult;
import com.veyru.application.messaging.ConversationService;
import com.veyru.application.messaging.MessageResult;
import com.veyru.application.messaging.PageResult;
import com.veyru.application.messaging.SendMessageCommand;
import com.veyru.application.messaging.SendMessageUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}")
public class MessageController {
  private final SendMessageUseCase sendMessage;
  private final ConversationService conversations;
  private final SimpUserRegistry userRegistry;

  public MessageController(
      SendMessageUseCase sendMessage,
      ConversationService conversations,
      SimpUserRegistry userRegistry) {
    this.sendMessage = sendMessage;
    this.conversations = conversations;
    this.userRegistry = userRegistry;
  }

  @GetMapping("/conversations")
  public ResponseEntity<List<ConversationResult>> getConversations(
      @AuthenticationPrincipal AppUserDetails user) {
    return ResponseEntity.ok(conversations.getConversations(user.getId()));
  }

  @GetMapping("/conversations/{conversationId}/messages")
  public ResponseEntity<PageResponse<MessageResult>> getMessages(
      @AuthenticationPrincipal AppUserDetails user,
      @PathVariable String conversationId,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
    PageResult<MessageResult> result =
        conversations.getMessages(conversationId, user.getId(), page, size);
    return ResponseEntity.ok(
        new PageResponse<>(
            result.items(),
            result.page(),
            result.size(),
            result.totalElements(),
            result.totalPages()));
  }

  @PostMapping("/conversations/{conversationId}/messages")
  public ResponseEntity<MessageResult> sendMessage(
      @AuthenticationPrincipal AppUserDetails user,
      @PathVariable String conversationId,
      @Valid @RequestBody SendMessageRequest request) {
    MessageResult result =
        sendMessage.execute(
            new SendMessageCommand(user.getId(), request.receiverId(), request.text(), null));
    return ResponseEntity.status(201).body(result);
  }

  @PutMapping("/conversations/{conversationId}/read-receipt")
  public ResponseEntity<Void> markAsRead(
      @AuthenticationPrincipal AppUserDetails user, @PathVariable String conversationId) {
    conversations.markMessagesAsRead(conversationId, user.getId());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/users/{userId}/presence")
  public ResponseEntity<Boolean> isUserOnline(@PathVariable String userId) {
    return ResponseEntity.ok(userRegistry.getUser(userId) != null);
  }

  @PostMapping("/presence-queries")
  public ResponseEntity<Map<String, Boolean>> getOnlineUsers(@RequestBody List<String> userIds) {
    return ResponseEntity.ok(
        userIds.stream()
            .collect(Collectors.toMap(id -> id, id -> userRegistry.getUser(id) != null)));
  }

  @PostMapping("/conversations")
  public ResponseEntity<Map<String, String>> startConversation(
      @AuthenticationPrincipal AppUserDetails user, @RequestParam String otherUserId) {
    String conversationId = conversations.getOrCreateConversation(user.getId(), otherUserId).id();
    return ResponseEntity.ok(Map.of("conversationId", conversationId));
  }
}
