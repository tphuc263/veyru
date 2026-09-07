package com.veyru.application.notification;

import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.CurrentActor;
import com.veyru.application.port.out.NotificationNotifier;
import com.veyru.application.port.out.NotificationStore;
import com.veyru.application.result.notification.NotificationResult;
import com.veyru.domain.enums.NotificationType;
import com.veyru.domain.model.Notification;
import com.veyru.domain.model.User;
import java.time.Clock;
import java.util.List;

public class NotificationService {
  private final NotificationStore notificationStore;
  private final NotificationNotifier notifier;
  private final AvatarCache userAvatarCacheService;
  private final Clock clock;
  private final CurrentActor currentActor;

  public void sendLikePhotoNotification(
      String photoOwnerId, User actor, String photoId, String thumbnailUrl) {
    if (actor.id().equals(photoOwnerId)) return;
    publishNotification(
        photoOwnerId,
        actor,
        NotificationType.LIKE_PHOTO,
        photoId,
        null,
        actor.username() + " đã thích ảnh của bạn",
        thumbnailUrl);
  }

  public void sendCommentPhotoNotification(
      String photoOwnerId, User actor, String photoId, String commentId, String thumbnailUrl) {
    if (actor.id().equals(photoOwnerId)) return;
    publishNotification(
        photoOwnerId,
        actor,
        NotificationType.COMMENT_PHOTO,
        photoId,
        commentId,
        actor.username() + " đã bình luận ảnh của bạn",
        thumbnailUrl);
  }

  public void sendLikeCommentNotification(
      String commentOwnerId, User actor, String photoId, String commentId) {
    if (actor.id().equals(commentOwnerId)) return;
    publishNotification(
        commentOwnerId,
        actor,
        NotificationType.LIKE_COMMENT,
        photoId,
        commentId,
        actor.username() + " đã thích bình luận của bạn",
        null);
  }

  public void sendReplyCommentNotification(
      String parentCommentOwnerId,
      User actor,
      String photoId,
      String commentId,
      String thumbnailUrl) {
    if (actor.id().equals(parentCommentOwnerId)) return;
    publishNotification(
        parentCommentOwnerId,
        actor,
        NotificationType.REPLY_COMMENT,
        photoId,
        commentId,
        actor.username() + " đã trả lời bình luận của bạn",
        thumbnailUrl);
  }

  public void sendMentionNotification(
      String mentionedUserId, User actor, String photoId, String commentId, String thumbnailUrl) {
    if (actor.id().equals(mentionedUserId)) return;
    publishNotification(
        mentionedUserId,
        actor,
        NotificationType.MENTION_IN_COMMENT,
        photoId,
        commentId,
        actor.username() + " đã nhắc đến bạn trong một bình luận",
        thumbnailUrl);
  }

  public void sendTagInPhotoNotification(
      String taggedUserId, User actor, String photoId, String thumbnailUrl) {
    if (actor.id().equals(taggedUserId)) return;
    publishNotification(
        taggedUserId,
        actor,
        NotificationType.TAG_IN_PHOTO,
        photoId,
        null,
        actor.username() + " đã gắn thẻ bạn trong một ảnh",
        thumbnailUrl);
  }

  public void sendNewFollowerNotification(String followedUserId, User actor) {
    if (actor.id().equals(followedUserId)) return;
    publishNotification(
        followedUserId,
        actor,
        NotificationType.NEW_FOLLOWER,
        null,
        null,
        actor.username() + " đã bắt đầu theo dõi bạn",
        null);
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

  public long getUnreadCount() {
    return getUnreadCount(requireActorId());
  }

  public void markAsRead(String notificationId, String recipientId) {
    notificationStore
        .findById(notificationId)
        .filter(notification -> notification.recipientId().equals(recipientId))
        .ifPresentOrElse(
            notification -> {
              notificationStore.save(notification.markedRead());
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
    notificationStore.saveAll(unreadNotifications.stream().map(Notification::markedRead).toList());
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
    Notification notification =
        Notification.create(
            recipientId,
            actor.id(),
            actor.username(),
            type,
            photoId,
            commentId,
            message,
            thumbnailUrl,
            clock.instant());
    Notification savedNotification = notificationStore.save(notification);
    notifier.send(recipientId, convertToResponse(savedNotification));
  }

  private NotificationResult convertToResponse(Notification notification) {
    return new NotificationResult(
        notification.id(),
        notification.type(),
        notification.message(),
        notification.read(),
        notification.createdAt(),
        notification.actorId(),
        notification.actor().username(),
        userAvatarCacheService.getAvatar(notification.actorId()),
        notification.photoId(),
        notification.commentId(),
        notification.thumbnailUrl());
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
