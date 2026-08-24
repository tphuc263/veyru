import api from '../config/ApiConfig';
import { PaginatedResponse, UnifiedPost } from '../types/api';

export const getNewsfeed = async (page: number = 0, size: number = 20): Promise<PaginatedResponse<UnifiedPost>> => {
        const response = await api.get<PaginatedResponse<UnifiedPost>>('/feed/unified', {
            params: { page, size }
        });
        return response.data;
};
