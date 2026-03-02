import api from '../config/ApiConfig';
import { PaginatedResponse, Photo } from '../types/api';

export const getUserPhotos = async (userId: number, page: number = 0, size: number = 20): Promise<PaginatedResponse<Photo>> => {
    try {
        const response = await api.get(`/photos/user/${userId}`, {
            params: { page, size }
        });
        return response.data as PaginatedResponse<Photo>;
    } catch (error: any) {
        throw new Error(`Failed to load user photos: ${error.message}`);
    }
};

export const createPhoto = async (photoData: FormData): Promise<Photo> => {
    try {
        const response = await api.post('/photos', photoData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            },
            timeout: 60000
        });
        return response.data ?? response;
    } catch (error: any) {
        throw new Error(`Failed to create photo: ${error.message}`);
    }
};

export const getPhotoById = async (photoId: string): Promise<any> => {
    try {
        const response = await api.get(`/photos/${photoId}`);
        return response.data;
    } catch (error: any) {
        throw new Error(`Failed to load photo details: ${error.message}`);
    }
};
