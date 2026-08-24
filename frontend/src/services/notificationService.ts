import api from "../config/ApiConfig";
import type { components } from '../types/generated-api';

export type Notification = components['schemas']['NotificationResponse'];

export const getNotifications = async (page: number = 0, size: number = 20): Promise<Notification[]> => {
  const response = await api.get<Notification[]>('/users/me/notifications', { params: { page, size } });
  return response.data;
};

export const getUnreadCount = async () => {
  const response = await api.get<components['schemas']['NotificationSummaryResponse']>('/users/me/notifications/summary');
  return response.data.unreadCount ?? 0;
};

export const markAsRead = async (notificationId: string) => {
  await api.patch(`/users/me/notifications/${notificationId}`);
};

export const markAllAsRead = async () => {
  await api.patch('/users/me/notifications');
};
