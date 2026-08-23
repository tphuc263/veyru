package com.veyru.application.notification;

import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.NotificationNotifier;
import com.veyru.application.port.out.NotificationStore;
import com.veyru.application.result.notification.NotificationResponse;
import com.veyru.domain.enums.NotificationType;
import com.veyru.application.error.ApiException;
import com.veyru.application.error.ErrorCode;
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
    publishNotification(
        photoOwnerId,
        actor,
        NotificationType.LIKE_PHOTO,
        photoId,
        null,
        actor.getUsername() + " đã thích ảnh của bạn",
        thumbnailUrl);
  }

  public void sendCommentPhotoNotification(
      String photoOwnerId, User actor, String photoId, String commentId, String thumbnailUrl) {
    if (actor.getId().equals(photoOwnerId)) return;
    publishNotification(photoOwnerId, actor, NotificationType.COMMENT_PHOTO, photoId, commentId,
        actor.getUsername() + " đã bình luận ảnh của bạn", thumbnailUrl);
  }

  public void sendLikeCommentNotification(
      String commentOwnerId, User actor, String photoId, String commentId) {
    if (actor.getId().equals(commentOwnerId)) return;
    publishNotification(commentOwnerId, actor, NotificationType.LIKE_COMMENT, photoId, commentId,
        actor.getUsername() + " đã thích bình luận của bạn", null);
  }

  public void sendReplyCommentNotification(
      String parentCommentOwnerId,
      User actor,
      String photoId,
      String commentId,
      String thumbnailUrl) {
    if (actor.getId().equals(parentCommentOwnerId)) return;
    publishNotification(parentCommentOwnerId, actor, NotificationType.REPLY_COMMENT, photoId, commentId,
        actor.getUsername() + " đã trả lời bình luận của bạn", thumbnailUrl);
  }

  public void sendMentionNotification(
      String mentionedUserId, User actor, String photoId, String commentId, String thumbnailUrl) {
    if (actor.getId().equals(mentionedUserId)) return;
    publishNotification(mentionedUserId, actor, NotificationType.MENTION_IN_COMMENT, photoId, commentId,
        actor.getUsername() + " đã nhắc đến bạn trong một bình luận", thumbnailUrl);
  }

  public void sendTagInPhotoNotification(
      String taggedUserId, User actor, String photoId, String thumbnailUrl) {
    if (actor.getId().equals(taggedUserId)) return;
    publishNotification(taggedUserId, actor, NotificationType.TAG_IN_PHOTO, photoId, null,
        actor.getUsername() + " đã gắn thẻ bạn trong một ảnh", thumbnailUrl);
  }

  public void sendNewFollowerNotification(String followedUserId, User actor) {
    if (actor.getId().equals(followedUserId)) return;
    publishNotification(followedUserId, actor, NotificationType.NEW_FOLLOWER, null, null,
        actor.getUsername() + " đã bắt đầu theo dõi bạn", null);
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

  private void publishNotification(
      String recipientId,
      User actor,
      NotificationType type,
      String photoId,
      String commentId,
      String message,
      String thumbnailUrl) {
    // Save to database
    Notification notification =
        Notification.builder()
            .recipientId(recipientId)
            .actorId(actor.getId())
            .type(type)
            .photoId(photoId)
            .commentId(commentId)
            .message(message)
            .read(false)
            .createdAt(Instant.now())
            .actor(Notification.EmbeddedActor.builder().username(actor.getUsername()).build())
            .thumbnailUrl(thumbnailUrl)
            .build();
    Notification savedNotification = notificationStore.save(notification);
    log.info("Saved notification: {} for user: {}", savedNotification.getId(), recipientId);
    // Send real-time notification via WebSocket
    sendRealTimeNotification(recipientId, convertToResponse(savedNotification));
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
