import api from '../config/ApiConfig';
import { PaginatedResponse, Photo } from '../types/api';

export const getNewsfeed = async (page: number = 0, size: number = 20): Promise<PaginatedResponse<Photo>> => {
    try {
        const response = await api.get('/newsfeed', {
            params: { page, size }
        });
        return response.data as PaginatedResponse<Photo>;
    } catch (error: any) {
        throw new Error(`Failed to load newsfeed: ${error.message}`);
    }
};
