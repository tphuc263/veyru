package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.request.message.SendMessageRequest;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.message.ConversationResponse;
import share_app.tphucshareapp.dto.response.message.MessageResponse;
import share_app.tphucshareapp.service.message.MessageService;
import share_app.tphucshareapp.service.message.SocketIOHandler;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;
    private final SocketIOHandler socketIOHandler;

    /**
     * Get all conversations for the current user
     */
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getConversations() {
        String userId = messageService.getCurrentUserId();
        List<ConversationResponse> conversations = messageService.getConversations(userId);
        return ResponseEntity.ok(ApiResponse.success(conversations, "Conversations retrieved successfully"));
    }

    /**
     * Get messages for a specific conversation
     */
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        String userId = messageService.getCurrentUserId();
        Page<MessageResponse> messages = messageService.getMessages(conversationId, userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(messages, "Messages retrieved successfully"));
    }

    /**
     * Send a message via REST (fallback when Socket.IO is not available)
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @RequestBody SendMessageRequest request) {
        String senderId = messageService.getCurrentUserId();
        MessageResponse message = messageService.sendMessage(senderId, request.getReceiverId(), request.getText());
        return ResponseEntity.ok(ApiResponse.success(message, "Message sent successfully"));
    }

    /**
     * Mark messages as read
     */
    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable String conversationId) {
        String userId = messageService.getCurrentUserId();
        messageService.markMessagesAsRead(conversationId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Messages marked as read"));
    }

    /**
     * Get unread message count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        String userId = messageService.getCurrentUserId();
        long count = messageService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count, "Unread count retrieved"));
    }

    /**
     * Check if a user is online
     */
    @GetMapping("/online/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> isUserOnline(@PathVariable String userId) {
        boolean online = socketIOHandler.isUserOnline(userId);
        return ResponseEntity.ok(ApiResponse.success(online, "Online status retrieved"));
    }

    /**
     * Start a conversation with a user (or get existing one)
     */
    @PostMapping("/conversations/start/{otherUserId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> startConversation(
            @PathVariable String otherUserId) {
        String userId = messageService.getCurrentUserId();
        var conversation = messageService.getOrCreateConversation(userId, otherUserId);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("conversationId", conversation.getId()),
                "Conversation started successfully"));
    }
}
