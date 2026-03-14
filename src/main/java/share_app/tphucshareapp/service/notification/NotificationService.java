package share_app.tphucshareapp.service.notification;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.config.RabbitMQConfig;
import share_app.tphucshareapp.dto.response.notification.NotificationResponse;
import share_app.tphucshareapp.enums.NotificationType;
import share_app.tphucshareapp.event.NotificationEvent;
import share_app.tphucshareapp.model.Notification;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.NotificationRepository;
import share_app.tphucshareapp.service.user.UserAvatarCacheService;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final RabbitTemplate rabbitTemplate;
    private final SocketIOServer socketIOServer;
    private final UserAvatarCacheService userAvatarCacheService;

    @Override
    public void sendLikePhotoNotification(String photoOwnerId, User actor, String photoId, String thumbnailUrl) {
        if (actor.getId().equals(photoOwnerId)) return; // Don't notify self
        
        NotificationEvent event = NotificationEvent.builder()
                .recipientId(photoOwnerId)
                .actorId(actor.getId())
                .actorUsername(actor.getUsername())
                .type(NotificationType.LIKE_PHOTO)
                .photoId(photoId)
                .message(actor.getUsername() + " đã thích ảnh của bạn")
                .thumbnailUrl(thumbnailUrl)
                .createdAt(Instant.now())
                .build();
        
        publishNotification(event);
    }

    @Override
    public void sendCommentPhotoNotification(String photoOwnerId, User actor, String photoId, String commentId, String thumbnailUrl) {
        if (actor.getId().equals(photoOwnerId)) return;
        
        NotificationEvent event = NotificationEvent.builder()
                .recipientId(photoOwnerId)
                .actorId(actor.getId())
                .actorUsername(actor.getUsername())
                .type(NotificationType.COMMENT_PHOTO)
                .photoId(photoId)
                .commentId(commentId)
                .message(actor.getUsername() + " đã bình luận ảnh của bạn")
                .thumbnailUrl(thumbnailUrl)
                .createdAt(Instant.now())
                .build();
        
        publishNotification(event);
    }

    @Override
    public void sendLikeCommentNotification(String commentOwnerId, User actor, String photoId, String commentId) {
        if (actor.getId().equals(commentOwnerId)) return;
        
        NotificationEvent event = NotificationEvent.builder()
                .recipientId(commentOwnerId)
                .actorId(actor.getId())
                .actorUsername(actor.getUsername())
                .type(NotificationType.LIKE_COMMENT)
                .photoId(photoId)
                .commentId(commentId)
                .message(actor.getUsername() + " đã thích bình luận của bạn")
                .createdAt(Instant.now())
                .build();
        
        publishNotification(event);
    }

    @Override
    public void sendReplyCommentNotification(String parentCommentOwnerId, User actor, String photoId, String commentId, String thumbnailUrl) {
        if (actor.getId().equals(parentCommentOwnerId)) return;
        
        NotificationEvent event = NotificationEvent.builder()
                .recipientId(parentCommentOwnerId)
                .actorId(actor.getId())
                .actorUsername(actor.getUsername())
                .type(NotificationType.REPLY_COMMENT)
                .photoId(photoId)
                .commentId(commentId)
                .message(actor.getUsername() + " đã trả lời bình luận của bạn")
                .thumbnailUrl(thumbnailUrl)
                .createdAt(Instant.now())
                .build();
        
        publishNotification(event);
    }

    @Override
    public void sendMentionNotification(String mentionedUserId, User actor, String photoId, String commentId, String thumbnailUrl) {
        if (actor.getId().equals(mentionedUserId)) return;
        
        NotificationEvent event = NotificationEvent.builder()
                .recipientId(mentionedUserId)
                .actorId(actor.getId())
                .actorUsername(actor.getUsername())
                .type(NotificationType.MENTION_IN_COMMENT)
                .photoId(photoId)
                .commentId(commentId)
                .message(actor.getUsername() + " đã nhắc đến bạn trong một bình luận")
                .thumbnailUrl(thumbnailUrl)
                .createdAt(Instant.now())
                .build();
        
        publishNotification(event);
    }

    @Override
    public void sendTagInPhotoNotification(String taggedUserId, User actor, String photoId, String thumbnailUrl) {
        if (actor.getId().equals(taggedUserId)) return;
        
        NotificationEvent event = NotificationEvent.builder()
                .recipientId(taggedUserId)
                .actorId(actor.getId())
                .actorUsername(actor.getUsername())
                .type(NotificationType.TAG_IN_PHOTO)
                .photoId(photoId)
                .message(actor.getUsername() + " đã gắn thẻ bạn trong một ảnh")
                .thumbnailUrl(thumbnailUrl)
                .createdAt(Instant.now())
                .build();
        
        publishNotification(event);
    }

    @Override
    public void sendNewFollowerNotification(String followedUserId, User actor) {
        if (actor.getId().equals(followedUserId)) return;
        
        NotificationEvent event = NotificationEvent.builder()
                .recipientId(followedUserId)
                .actorId(actor.getId())
                .actorUsername(actor.getUsername())
                .type(NotificationType.NEW_FOLLOWER)
                .message(actor.getUsername() + " đã bắt đầu theo dõi bạn")
                .createdAt(Instant.now())
                .build();
        
        publishNotification(event);
    }

    @Override
    public List<NotificationResponse> getNotifications(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        
        return notifications.getContent().stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public long getUnreadCount(String userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    @Override
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Override
    public void markAllAsRead(String userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientIdAndReadFalseOrderByCreatedAtDesc(userId);
        
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    // Publish notification to RabbitMQ
    private void publishNotification(NotificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    event
            );
            log.info("Published notification event: {} for user: {}", event.getType(), event.getRecipientId());
        } catch (Exception e) {
            log.error("Failed to publish notification event", e);
            // Fallback: save directly if RabbitMQ is unavailable
            processNotificationEvent(event);
        }
    }

    // Listen for notification events from RabbitMQ
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consumeNotification(NotificationEvent event) {
        log.info("Consumed notification event: {} for user: {}", event.getType(), event.getRecipientId());
        processNotificationEvent(event);
    }

    // Process notification event
    private void processNotificationEvent(NotificationEvent event) {
        // Save to database
        Notification notification = Notification.builder()
                .recipientId(event.getRecipientId())
                .actorId(event.getActorId())
                .type(event.getType())
                .photoId(event.getPhotoId())
                .commentId(event.getCommentId())
                .message(event.getMessage())
                .read(false)
                .createdAt(event.getCreatedAt())
                .actor(Notification.EmbeddedActor.builder()
                        .username(event.getActorUsername())
                        .build())
                .thumbnailUrl(event.getThumbnailUrl())
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Saved notification: {} for user: {}", savedNotification.getId(), event.getRecipientId());
        
        // Send real-time notification via Socket.IO
        sendRealTimeNotification(event.getRecipientId(), convertToResponse(savedNotification));
    }

    // Send real-time notification via Socket.IO
    private void sendRealTimeNotification(String userId, NotificationResponse response) {
        try {
            socketIOServer.getRoomOperations(userId).sendEvent("notification", response);
            log.info("Sent real-time notification to user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send real-time notification to user: {}", userId, e);
        }
    }

    private NotificationResponse convertToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .actorId(notification.getActorId())
                .actorUsername(notification.getActor() != null ? notification.getActor().getUsername() : null)
                .actorImageUrl(userAvatarCacheService.getAvatar(notification.getActorId()))
                .photoId(notification.getPhotoId())
                .commentId(notification.getCommentId())
                .thumbnailUrl(notification.getThumbnailUrl())
                .build();
    }
}
