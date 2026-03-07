import api from "../config/ApiConfig";

export interface Notification {
  id: string;
  type: string;
  message: string;
  read: boolean;
  createdAt: string;
  actorId: string;
  actorUsername: string;
  actorImageUrl: string;
  photoId?: string;
  commentId?: string;
  thumbnailUrl?: string;
}

export const getNotifications = async (page: number = 0, size: number = 20) => {
  try {
    return await api.get(`/notifications?page=${page}&size=${size}`);
  } catch (error: any) {
    throw new Error(`Failed to load notifications: ${error.message}`);
  }
};

export const getUnreadCount = async () => {
  try {
    return await api.get(`/notifications/unread-count`);
  } catch (error: any) {
    throw new Error(`Failed to get unread count: ${error.message}`);
  }
};

export const markAsRead = async (notificationId: string) => {
  try {
    return await api.put(`/notifications/${notificationId}/read`);
  } catch (error: any) {
    throw new Error(`Failed to mark notification as read: ${error.message}`);
  }
};

export const markAllAsRead = async () => {
  try {
    return await api.put(`/notifications/read-all`);
  } catch (error: any) {
    throw new Error(`Failed to mark all notifications as read: ${error.message}`);
  }
};
