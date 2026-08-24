import api from "../config/ApiConfig";
import type { components } from '../types/generated-api';

type LikeResponse = components['schemas']['LikeResponse'];

export const likePhoto = async (photoId: string): Promise<void> => {
  await api.put(`/photos/${photoId}/likes/me`);
};

export const unlikePhoto = async (photoId: string): Promise<void> => {
  await api.delete(`/photos/${photoId}/likes/me`);
};

export const toggleLike = async (photoId: string, currentlyLiked?: boolean) => {
  return currentlyLiked ? unlikePhoto(photoId) : likePhoto(photoId);
};

export const getPhotoLikes = async (photoId: string): Promise<LikeResponse[]> => {
  const response = await api.get<LikeResponse[]>(`/photos/${photoId}/likes`);
  return response.data;
};

export const getPhotoLikesCount = async (photoId: string): Promise<number> => {
  const response = await api.get<number>(`/photos/${photoId}/likes/count`);
  return response.data;
};
