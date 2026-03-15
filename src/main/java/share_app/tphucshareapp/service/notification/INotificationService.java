package share_app.tphucshareapp.service.notification;

import share_app.tphucshareapp.dto.response.notification.NotificationResponse;
import share_app.tphucshareapp.model.User;

import java.util.List;

public interface INotificationService {
    
    /**
     * Send notification for photo like
     */
    void sendLikePhotoNotification(String photoOwnerId, User actor, String photoId, String thumbnailUrl);
    
    /**
     * Send notification for photo comment
     */
    void sendCommentPhotoNotification(String photoOwnerId, User actor, String photoId, String commentId, String thumbnailUrl);
    
    /**
     * Send notification for comment like
     */
    void sendLikeCommentNotification(String commentOwnerId, User actor, String photoId, String commentId);
    
    /**
     * Send notification for comment reply
     */
    void sendReplyCommentNotification(String parentCommentOwnerId, User actor, String photoId, String commentId, String thumbnailUrl);
    
    /**
     * Send notification for mention in comment
     */
    void sendMentionNotification(String mentionedUserId, User actor, String photoId, String commentId, String thumbnailUrl);
    
    /**
     * Send notification for user tag in photo
     */
    void sendTagInPhotoNotification(String taggedUserId, User actor, String photoId, String thumbnailUrl);
    
    /**
     * Send notification for new follower
     */
    void sendNewFollowerNotification(String followedUserId, User actor);
    
    /**
     * Get notifications for user
     */
    List<NotificationResponse> getNotifications(String userId, int page, int size);
    
    /**
     * Get unread notifications count
     */
    long getUnreadCount(String userId);
    
    /**
     * Mark notification as read
     */
    void markAsRead(String notificationId);
    
    /**
     * Mark all notifications as read
     */
    void markAllAsRead(String userId);
}
