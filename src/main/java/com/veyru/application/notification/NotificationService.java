package com.veyru.application.notification;

import com.veyru.application.event.NotificationEvent;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.NotificationNotifier;
import com.veyru.application.port.out.NotificationStore;
import com.veyru.application.result.notification.NotificationResponse;
import com.veyru.domain.enums.NotificationType;
import com.veyru.domain.exception.ApiException;
import com.veyru.domain.exception.ErrorCode;
import com.veyru.domain.model.Notification;
import com.veyru.domain.model.User;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationService {
  private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
  private final NotificationStore notificationStore;
  private final NotificationNotifier notifier;
  private final AvatarCache userAvatarCacheService;

  public void sendLikePhotoNotification(
      String photoOwnerId, User actor, String photoId, String thumbnailUrl) {
    if (actor.getId().equals(photoOwnerId)) return; // Don't notify self
    NotificationEvent event =
        new NotificationEvent(
            photoOwnerId,
            actor.getId(),
            actor.getUsername(),
            NotificationType.LIKE_PHOTO,
            photoId,
            null,
            actor.getUsername() + " đã thích ảnh của bạn",
            thumbnailUrl,
            Instant.now());
    publishNotification(event);
  }

  public void sendCommentPhotoNotification(
      String photoOwnerId, User actor, String photoId, String commentId, String thumbnailUrl) {
    if (actor.getId().equals(photoOwnerId)) return;
    NotificationEvent event =
        new NotificationEvent(
            photoOwnerId,
            actor.getId(),
            actor.getUsername(),
            NotificationType.COMMENT_PHOTO,
            photoId,
            commentId,
            actor.getUsername() + " đã bình luận ảnh của bạn",
            thumbnailUrl,
            Instant.now());
    publishNotification(event);
  }

  public void sendLikeCommentNotification(
      String commentOwnerId, User actor, String photoId, String commentId) {
    if (actor.getId().equals(commentOwnerId)) return;
    NotificationEvent event =
        new NotificationEvent(
            commentOwnerId,
            actor.getId(),
            actor.getUsername(),
            NotificationType.LIKE_COMMENT,
            photoId,
            commentId,
            actor.getUsername() + " đã thích bình luận của bạn",
            null,
            Instant.now());
    publishNotification(event);
  }

  public void sendReplyCommentNotification(
      String parentCommentOwnerId,
      User actor,
      String photoId,
      String commentId,
      String thumbnailUrl) {
    if (actor.getId().equals(parentCommentOwnerId)) return;
    NotificationEvent event =
        new NotificationEvent(
            parentCommentOwnerId,
            actor.getId(),
            actor.getUsername(),
            NotificationType.REPLY_COMMENT,
            photoId,
            commentId,
            actor.getUsername() + " đã trả lời bình luận của bạn",
            thumbnailUrl,
            Instant.now());
    publishNotification(event);
  }

  public void sendMentionNotification(
      String mentionedUserId, User actor, String photoId, String commentId, String thumbnailUrl) {
    if (actor.getId().equals(mentionedUserId)) return;
    NotificationEvent event =
        new NotificationEvent(
            mentionedUserId,
            actor.getId(),
            actor.getUsername(),
            NotificationType.MENTION_IN_COMMENT,
            photoId,
            commentId,
            actor.getUsername() + " đã nhắc đến bạn trong một bình luận",
            thumbnailUrl,
            Instant.now());
    publishNotification(event);
  }

  public void sendTagInPhotoNotification(
      String taggedUserId, User actor, String photoId, String thumbnailUrl) {
    if (actor.getId().equals(taggedUserId)) return;
    NotificationEvent event =
        new NotificationEvent(
            taggedUserId,
            actor.getId(),
            actor.getUsername(),
            NotificationType.TAG_IN_PHOTO,
            photoId,
            null,
            actor.getUsername() + " đã gắn thẻ bạn trong một ảnh",
            thumbnailUrl,
            Instant.now());
    publishNotification(event);
  }

  public void sendNewFollowerNotification(String followedUserId, User actor) {
    if (actor.getId().equals(followedUserId)) return;
    NotificationEvent event =
        new NotificationEvent(
            followedUserId,
            actor.getId(),
            actor.getUsername(),
            NotificationType.NEW_FOLLOWER,
            null,
            null,
            actor.getUsername() + " đã bắt đầu theo dõi bạn",
            null,
            Instant.now());
    publishNotification(event);
  }

  public List<NotificationResponse> getNotifications(String userId, int page, int size) {
    return notificationStore.findByRecipient(userId, page, size).stream()
        .map(this::convertToResponse)
        .toList();
  }

  public long getUnreadCount(String userId) {
    return notificationStore.countUnread(userId);
  }

  public void markAsRead(String notificationId, String recipientId) {
    notificationStore
        .findById(notificationId)
        .filter(notification -> notification.getRecipientId().equals(recipientId))
        .ifPresentOrElse(
            notification -> {
              notification.setRead(true);
              notificationStore.save(notification);
            },
            () -> {
              throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
            });
  }

  public void markAllAsRead(String userId) {
    List<Notification> unreadNotifications = notificationStore.findUnread(userId);
    unreadNotifications.forEach(notification -> notification.setRead(true));
    notificationStore.saveAll(unreadNotifications);
  }

  private void publishNotification(NotificationEvent event) {
    processNotificationEvent(event);
  }

  // Process notification event
  private void processNotificationEvent(NotificationEvent event) {
    // Save to database
    Notification notification =
        Notification.builder()
            .recipientId(event.recipientId())
            .actorId(event.actorId())
            .type(event.type())
            .photoId(event.photoId())
            .commentId(event.commentId())
            .message(event.message())
            .read(false)
            .createdAt(event.createdAt())
            .actor(Notification.EmbeddedActor.builder().username(event.actorUsername()).build())
            .thumbnailUrl(event.thumbnailUrl())
            .build();
    Notification savedNotification = notificationStore.save(notification);
    log.info("Saved notification: {} for user: {}", savedNotification.getId(), event.recipientId());
    // Send real-time notification via WebSocket
    sendRealTimeNotification(event.recipientId(), convertToResponse(savedNotification));
  }

  // Send real-time notification via WebSocket
  private void sendRealTimeNotification(String userId, NotificationResponse response) {

    notifier.send(userId, response);
    log.info("Sent real-time notification to user: {}", userId);
  }

  private NotificationResponse convertToResponse(Notification notification) {
    return NotificationResponse.builder()
        .id(notification.getId())
        .type(notification.getType())
        .message(notification.getMessage())
        .read(notification.isRead())
        .createdAt(notification.getCreatedAt())
        .actorId(notification.getActorId())
        .actorUsername(
            notification.getActor() != null ? notification.getActor().getUsername() : null)
        .actorImageUrl(userAvatarCacheService.getAvatar(notification.getActorId()))
        .photoId(notification.getPhotoId())
        .commentId(notification.getCommentId())
        .thumbnailUrl(notification.getThumbnailUrl())
        .build();
  }

  public NotificationService(
      NotificationStore notificationStore,
      NotificationNotifier notifier,
      AvatarCache userAvatarCacheService) {
    this.notificationStore = notificationStore;
    this.notifier = notifier;
    this.userAvatarCacheService = userAvatarCacheService;
  }
}
