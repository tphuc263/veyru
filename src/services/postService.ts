import api from '../config/ApiConfig';
import { PaginatedResponse, UnifiedPost } from '../types/api';

export const getUserPosts = async (userId: string, page: number = 0, size: number = 20): Promise<PaginatedResponse<UnifiedPost>> => {
        const response = await api.get<PaginatedResponse<UnifiedPost>>('/posts', {
            params: { userId, page, size }
        });
        return response.data;
};
