import api from "../config/ApiConfig";

export const toggleLike = async (photoId: string) => {
  try {
    return await api.post(`/likes/toggle/photo/${photoId}`);
  } catch (error: any) {
    throw new Error(`Failed to toggle like: ${error.message}`);
  }
};
