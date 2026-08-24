import api from '../config/ApiConfig';
import type { components } from '../types/generated-api';

type PhotoResponse = components['schemas']['PhotoResponse'];
type ShareResponse = components['schemas']['ShareResponse'];
type UserShares = components['schemas']['PageResponseShareWithPhotoResponse'];

export const sharePhoto = async (photoId: string, caption?: string): Promise<PhotoResponse> => {
    const response = await api.post<PhotoResponse>(`/photos/${photoId}/shares`, { caption });
    return response.data;
};

export const getPhotoShares = async (photoId: string): Promise<ShareResponse[]> => {
    const response = await api.get<ShareResponse[]>(`/photos/${photoId}/shares`);
    return response.data;
};

export const getShareCount = async (photoId: string): Promise<number> => {
    const response = await api.get<number>(`/photos/${photoId}/shares/count`);
    return response.data;
};

export const checkHasShared = async (photoId: string): Promise<boolean> => {
    const response = await api.get<boolean>(`/photos/${photoId}/shares/me`);
    return response.data;
};

export const getUserShares = async (userId: string, page: number = 0, size: number = 20): Promise<UserShares> => {
        const response = await api.get<UserShares>(`/users/${userId}/shares`, {
            params: { page, size }
        });
        return response.data;
};
