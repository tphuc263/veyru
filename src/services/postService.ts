import api from '../config/ApiConfig';
import { PaginatedResponse, UnifiedPost } from '../types/api';

export const getUserPosts = async (userId: string, page: number = 0, size: number = 20): Promise<PaginatedResponse<UnifiedPost>> => {
    try {
        const response = await api.get(`/posts/user/${userId}`, {
            params: { page, size }
        });
        return response.data as PaginatedResponse<UnifiedPost>;
    } catch (error: any) {
        throw new Error(`Failed to load user posts: ${error.message}`);
    }
};

