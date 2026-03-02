import api from "../config/ApiConfig";

export const getPhotoComments = async (photoId: string) => {
  try {
    return await api.get(`/comments/photo/${photoId}`);
  } catch (error: any) {
    throw new Error(`Failed to load comments: ${error.message}`);
  }
};

export const createComment = async (photoId: string, commentData: { content: string }) => {
  try {
    return await api.post(`/comments/photo/${photoId}`, { text: commentData.content });
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
