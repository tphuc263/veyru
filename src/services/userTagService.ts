import api from "../config/ApiConfig";

export interface UserTagRequest {
  taggedUserId: string;
  positionX?: number;
  positionY?: number;
}

export interface UserTagResponse {
  id: string;
  photoId: string;
  taggedUserId: string;
  taggedByUserId: string;
  username: string;
  userImageUrl: string;
  positionX?: number;
  positionY?: number;
  createdAt: string;
}

export const tagUserInPhoto = async (photoId: string, tagData: UserTagRequest) => {
  try {
    return await api.post(`/user-tags/photo/${photoId}`, tagData);
  } catch (error: any) {
    throw new Error(`Failed to tag user: ${error.message}`);
  }
};

export const removeUserTag = async (photoId: string, taggedUserId: string) => {
  try {
    return await api.delete(`/user-tags/photo/${photoId}/user/${taggedUserId}`);
  } catch (error: any) {
    throw new Error(`Failed to remove user tag: ${error.message}`);
  }
};

export const getPhotoUserTags = async (photoId: string) => {
  try {
    return await api.get(`/user-tags/photo/${photoId}`);
  } catch (error: any) {
    throw new Error(`Failed to get photo user tags: ${error.message}`);
  }
};

export const getPhotosWhereUserIsTagged = async (userId: string) => {
  try {
    return await api.get(`/user-tags/user/${userId}/tagged-photos`);
  } catch (error: any) {
    throw new Error(`Failed to get tagged photos: ${error.message}`);
  }
};
