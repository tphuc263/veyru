import api from '../config/ApiConfig';
import { PaginatedResponse, Photo } from '../types/api';

export const getExploreFeed = async (page: number = 0, size: number = 20): Promise<PaginatedResponse<Photo>> => {
    try {
        const response = await api.get('/explore', {
            params: { page, size }
        });
        return response.data as PaginatedResponse<Photo>;
    } catch (error: any) {
        throw new Error(`Failed to load explore feed: ${error.message}`);
    }
};

export const getPopularPhotos = async (page: number = 0, size: number = 20): Promise<PaginatedResponse<Photo>> => {
    try {
        const response = await api.get('/explore/popular', {
            params: { page, size }
        });
        return response.data as PaginatedResponse<Photo>;
    } catch (error: any) {
        throw new Error(`Failed to load popular photos: ${error.message}`);
    }
};

export const getPhotosByTag = async (tag: string, page: number = 0, size: number = 20): Promise<PaginatedResponse<Photo>> => {
    try {
        const response = await api.get(`/explore/tags/${encodeURIComponent(tag)}`, {
            params: { page, size }
        });
        return response.data as PaginatedResponse<Photo>;
    } catch (error: any) {
        throw new Error(`Failed to load photos for tag: ${error.message}`);
    }
};
