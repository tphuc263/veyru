import api from '../config/ApiConfig';
import { PaginatedResponse, Photo } from '../types/api';

export const getUserPhotos = async (userId: string | number, page: number = 0, size: number = 20): Promise<PaginatedResponse<Photo>> => {
        const response = await api.get<PaginatedResponse<Photo>>('/photos', {
            params: { userId, page, size }
        });
        return response.data;
};

export const createPhoto = async (photoData: FormData): Promise<Photo> => {
        const response = await api.post<Photo>('/photos', photoData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            },
            timeout: 60000
        });
        return response.data;
};

export const getPhotoById = async (photoId: string): Promise<Photo> => {
    const response = await api.get<Photo>(`/photos/${photoId}`);
    return response.data;
};

export const deletePhoto = async (photoId: string): Promise<void> => {
    await api.delete(`/photos/${photoId}`);
};
