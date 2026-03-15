package share_app.tphucshareapp.service.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.response.message.ConversationResponse;
import share_app.tphucshareapp.dto.response.message.MessageResponse;
import share_app.tphucshareapp.exceptions.ResourceNotFoundException;
import share_app.tphucshareapp.model.Conversation;
import share_app.tphucshareapp.model.Message;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.ConversationRepository;
import share_app.tphucshareapp.repository.MessageRepository;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.security.userdetails.AppUserDetails;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    /**
     * Send a message from current user to receiver
     */
    public MessageResponse sendMessage(String senderId, String receiverId, String text) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

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

        return toMessageResponse(message);
    }

    /**
     * Get all conversations for the current user
     */
    public List<ConversationResponse> getConversations(String userId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "lastMessageAt");
        List<Conversation> conversations = conversationRepository.findByParticipantIdsContaining(userId, sort);

        List<ConversationResponse> responses = new ArrayList<>();
        for (Conversation conv : conversations) {
            String otherUserId = conv.getParticipantIds().stream()
                    .filter(id -> !id.equals(userId))
                    .findFirst()
                    .orElse(userId);

            User otherUser = userRepository.findById(otherUserId).orElse(null);
            if (otherUser == null) continue;

            long unreadCount = messageRepository.countByConversationIdAndReceiverIdAndReadFalse(
                    conv.getId(), userId);

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

    /**
     * Get messages for a specific conversation
     */
    public Page<MessageResponse> getMessages(String conversationId, String userId, int page, int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        // Verify user is a participant
        if (!conversation.getParticipantIds().contains(userId)) {
            throw new RuntimeException("User is not a participant of this conversation");
        }

        Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(
                conversationId, PageRequest.of(page, size));

        return messages.map(this::toMessageResponse);
    }

    /**
     * Mark messages as read
     */
    public void markMessagesAsRead(String conversationId, String userId) {
        Query query = new Query(Criteria.where("conversationId").is(conversationId)
                .and("receiverId").is(userId)
                .and("read").is(false));
        Update update = new Update().set("read", true);
        mongoTemplate.updateMulti(query, update, Message.class);
    }

    /**
     * Get total unread count for a user
     */
    public long getUnreadCount(String userId) {
        return messageRepository.countByReceiverIdAndReadFalse(userId);
    }

    /**
     * Get or create a conversation between two users.
     * Synchronized to prevent race conditions creating duplicate conversations.
     */
    public synchronized Conversation getOrCreateConversation(String userId1, String userId2) {
        // Try to find existing conversation
        List<Conversation> convs = conversationRepository.findByParticipantIdsContaining(
                userId1, Sort.unsorted());
        
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

    /**
     * Get the current authenticated user ID
     */
    public String getCurrentUserId() {
        AppUserDetails userDetails = (AppUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
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
