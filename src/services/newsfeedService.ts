import api from '../config/ApiConfig';
import { PaginatedResponse, UnifiedPost } from '../types/api';

export const getNewsfeed = async (page: number = 0, size: number = 20): Promise<PaginatedResponse<UnifiedPost>> => {
    try {
        // Use unified endpoint to get both photos and shares
        const response = await api.get('/newsfeed/unified', {
            params: { page, size }
        });
        return response.data as PaginatedResponse<UnifiedPost>;
    } catch (error: any) {
        throw new Error(`Failed to load newsfeed: ${error.message}`);
    }
};
