import api from "../config/ApiConfig";

export interface CommentData {
  content: string;
  parentCommentId?: string;
  mentionedUsernames?: string[];
}

export const getPhotoComments = async (photoId: string) => {
  try {
    return await api.get(`/comments/photo/${photoId}`);
  } catch (error: any) {
    throw new Error(`Failed to load comments: ${error.message}`);
  }
};

export const getCommentReplies = async (commentId: string, page: number = 0, size: number = 20) => {
  try {
    return await api.get(`/comments/${commentId}/replies?page=${page}&size=${size}`);
  } catch (error: any) {
    throw new Error(`Failed to load replies: ${error.message}`);
  }
};

export const createComment = async (photoId: string, commentData: CommentData) => {
  try {
    return await api.post(`/comments/photo/${photoId}`, { 
      text: commentData.content,
      parentCommentId: commentData.parentCommentId,
      mentionedUsernames: commentData.mentionedUsernames
    });
  } catch (error: any) {
    throw new Error(`Failed to create comment: ${error.message}`);
  }
};

export const updateComment = async (commentId: string, commentData: { content: string }) => {
  try {
    return await api.put(`/comments/${commentId}`, { text: commentData.content });
  } catch (error: any) {
    throw new Error(`Failed to update comment: ${error.message}`);
  }
};

export const deleteComment = async (commentId: string) => {
  try {
    return await api.delete(`/comments/${commentId}`);
  } catch (error: any) {
    throw new Error(`Failed to delete comment: ${error.message}`);
  }
};

export const toggleCommentLike = async (commentId: string) => {
  try {
    return await api.post(`/comments/${commentId}/like`);
  } catch (error: any) {
    throw new Error(`Failed to toggle comment like: ${error.message}`);
  }
};
