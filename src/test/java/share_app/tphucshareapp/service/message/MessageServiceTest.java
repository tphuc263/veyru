package share_app.tphucshareapp.service.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import share_app.tphucshareapp.model.Conversation;
import share_app.tphucshareapp.model.Message;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.ConversationRepository;
import share_app.tphucshareapp.repository.MessageRepository;
import share_app.tphucshareapp.repository.UserRepository;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private MessageService messageService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void sendMessage_WithDuplicateClientMessageId_ShouldSaveOnlyOnce() {
        String senderId = "sender123";
        String receiverId = "receiver456";
        String text = "Hello World";
        String clientMessageId = "client-uuid-999";
        String redisKey = "msg_idempotency:" + clientMessageId;

        User sender = new User();
        sender.setId(senderId);

        User receiver = new User();
        receiver.setId(receiverId);

        Conversation conversation = new Conversation();
        conversation.setId("conv789");
        conversation.setParticipantIds(java.util.List.of(senderId, receiverId));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // --- FIRST CALL (New Message) ---
        when(valueOperations.setIfAbsent(redisKey, "1", 24, TimeUnit.HOURS)).thenReturn(true);
        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));

        when(conversationRepository.findByParticipantIdsContaining(eq(senderId), any()))
                .thenReturn(Collections.singletonList(conversation));

        Message savedMessage = new Message();
        savedMessage.setId("msg111");
        savedMessage.setConversationId(conversation.getId());
        savedMessage.setSenderId(senderId);
        savedMessage.setReceiverId(receiverId);
        savedMessage.setText(text);
        savedMessage.setCreatedAt(Instant.now());
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        messageService.sendMessage(senderId, receiverId, text, clientMessageId);

        // --- SECOND CALL (Duplicate Message) ---
        when(valueOperations.setIfAbsent(redisKey, "1", 24, TimeUnit.HOURS)).thenReturn(false);

        messageService.sendMessage(senderId, receiverId, text, clientMessageId);

        // --- ASSERTIONS ---
        // Verify Redis was checked twice
        verify(valueOperations, times(2)).setIfAbsent(redisKey, "1", 24, TimeUnit.HOURS);

        // Verify DB logic was executed ONLY ONCE
        verify(userRepository, times(1)).findById(senderId);
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(conversationRepository, times(1)).save(any(Conversation.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq(receiverId), eq("/queue/messages"), any());
    }
}
