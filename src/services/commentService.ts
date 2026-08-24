import api from "../config/ApiConfig";
import type { components } from '../types/generated-api';

type CommentResponse = components['schemas']['CommentResponse'];

export interface CommentData {
  content: string;
  parentCommentId?: string;
  mentionedUsernames?: string[];
}

export const getPhotoComments = async (photoId: string): Promise<CommentResponse[]> => {
  const response = await api.get<CommentResponse[]>(`/photos/${photoId}/comments`);
  return response.data;
};

export const getCommentReplies = async (commentId: string, page: number = 0, size: number = 20): Promise<CommentResponse[]> => {
  const response = await api.get<CommentResponse[]>(`/comments/${commentId}/replies`, { params: { page, size } });
  return response.data;
};

export const createComment = async (photoId: string, commentData: CommentData): Promise<CommentResponse> => {
    const response = await api.post<CommentResponse>(`/photos/${photoId}/comments`, {
      text: commentData.content,
      parentCommentId: commentData.parentCommentId,
      mentionedUsernames: commentData.mentionedUsernames
    });
    return response.data;
};

export const updateComment = async (commentId: string, commentData: { content: string }): Promise<CommentResponse> => {
  const response = await api.patch<CommentResponse>(`/comments/${commentId}`, { text: commentData.content });
  return response.data;
};

export const deleteComment = async (commentId: string) => {
  await api.delete(`/comments/${commentId}`);
};

export const toggleCommentLike = async (commentId: string, currentlyLiked: boolean) => {
  const response = currentlyLiked
    ? await api.delete<CommentResponse>(`/comments/${commentId}/likes/me`)
    : await api.put<CommentResponse>(`/comments/${commentId}/likes/me`);
  return response.data;
};
