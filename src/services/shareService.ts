import api from '../config/ApiConfig';

export const sharePhoto = async (photoId: string, caption?: string): Promise<any> => {
    try {
        const response = await api.post(`/shares/photo/${photoId}`, { caption });
        return response;
    } catch (error: any) {
        throw new Error(`Failed to share photo: ${error.message}`);
    }
};

export const getPhotoShares = async (photoId: string): Promise<any[]> => {
    try {
        const response = await api.get(`/shares/photo/${photoId}`);
        return response.data as any[];
    } catch (error: any) {
        throw new Error(`Failed to get photo shares: ${error.message}`);
    }
};

export const getShareCount = async (photoId: string): Promise<number> => {
    try {
        const response = await api.get(`/shares/photo/${photoId}/count`);
        return response.data as number;
    } catch (error: any) {
        throw new Error(`Failed to get share count: ${error.message}`);
    }
};

export const checkHasShared = async (photoId: string): Promise<boolean> => {
    try {
        const response = await api.get(`/shares/photo/${photoId}/check`);
        return response.data as boolean;
    } catch (error: any) {
        throw new Error(`Failed to check share status: ${error.message}`);
    }
};
