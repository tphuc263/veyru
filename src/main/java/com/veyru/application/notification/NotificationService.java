package com.veyru.application.notification;

import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.NotificationNotifier;
import com.veyru.application.port.out.NotificationStore;
import com.veyru.application.port.out.CurrentActor;
import com.veyru.application.result.notification.NotificationResult;
import com.veyru.domain.enums.NotificationType;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.common.error.UseCaseError;
import com.veyru.domain.model.Notification;
import com.veyru.domain.model.User;
import java.time.Instant;
import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationService {
  private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
  private final NotificationStore notificationStore;
  private final NotificationNotifier notifier;
  private final AvatarCache userAvatarCacheService;
  private final Clock clock;
  private final CurrentActor currentActor;

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

  public List<NotificationResult> getNotifications(String userId, int page, int size) {
    return notificationStore.findByRecipient(userId, page, size).stream()
        .map(this::convertToResponse)
        .toList();
  }

  public List<NotificationResult> getNotifications(int page, int size) {
    return getNotifications(requireActorId(), page, size);
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
              notification.markRead();
              notificationStore.save(notification);
            },
            () -> {
              throw new UseCaseException(UseCaseError.RESOURCE_NOT_FOUND);
            });
  }

  public void markAsRead(String notificationId) {
    markAsRead(notificationId, requireActorId());
  }

  public void markAllAsRead(String userId) {
    List<Notification> unreadNotifications = notificationStore.findUnread(userId);
    unreadNotifications.forEach(Notification::markRead);
    notificationStore.saveAll(unreadNotifications);
  }

  public void markAllAsRead() {
    markAllAsRead(requireActorId());
  }

  private String requireActorId() {
    return currentActor
        .id()
        .orElseThrow(() -> new UseCaseException(UseCaseError.AUTHENTICATION_REQUIRED));
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
        Notification.create(
            recipientId,
            actor.getId(),
            actor.getUsername(),
            type,
            photoId,
            commentId,
            message,
            thumbnailUrl,
            clock.instant());
    Notification savedNotification = notificationStore.save(notification);
    log.info("Saved notification: {} for user: {}", savedNotification.getId(), recipientId);
    // Send real-time notification via WebSocket
    sendRealTimeNotification(recipientId, convertToResponse(savedNotification));
  }

  // Send real-time notification via WebSocket
  private void sendRealTimeNotification(String userId, NotificationResult response) {

    notifier.send(userId, response);
    log.info("Sent real-time notification to user: {}", userId);
  }

  private NotificationResult convertToResponse(Notification notification) {
    return new NotificationResult(
        notification.getId(), notification.getType(), notification.getMessage(), notification.isRead(),
        notification.getCreatedAt(), notification.getActorId(),
        notification.getActor() == null ? null : notification.getActor().getUsername(),
        userAvatarCacheService.getAvatar(notification.getActorId()), notification.getPhotoId(),
        notification.getCommentId(), notification.getThumbnailUrl());
  }

  public NotificationService(
      NotificationStore notificationStore,
      NotificationNotifier notifier,
      AvatarCache userAvatarCacheService,
      Clock clock,
      CurrentActor currentActor) {
    this.notificationStore = notificationStore;
    this.notifier = notifier;
    this.userAvatarCacheService = userAvatarCacheService;
    this.clock = clock;
    this.currentActor = currentActor;
  }
}
