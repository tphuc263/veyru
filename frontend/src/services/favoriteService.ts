import api from '../config/ApiConfig';
import type { components } from '../types/generated-api';

type PhotoResponse = components['schemas']['PhotoResponse'];

export const favoritePhoto = async (photoId: string): Promise<void> => {
    await api.put(`/users/me/favorites/${photoId}`);
};

export const unfavoritePhoto = async (photoId: string): Promise<void> => {
    await api.delete(`/users/me/favorites/${photoId}`);
};

export const toggleFavorite = async (photoId: string, currentlyFavorited?: boolean): Promise<void> => {
    return currentlyFavorited ? unfavoritePhoto(photoId) : favoritePhoto(photoId);
};

export const getFavorites = async (page: number = 0, size: number = 20): Promise<PhotoResponse[]> => {
        const response = await api.get<PhotoResponse[]>('/users/me/favorites', {
            params: { page, size }
        });
        return response.data;
};

export const checkFavorite = async (photoId: string): Promise<boolean> => {
    const response = await api.get<boolean>(`/users/me/favorites/${photoId}`);
    return response.data;
};
