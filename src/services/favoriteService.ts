import api from '../config/ApiConfig';

export const toggleFavorite = async (photoId: string): Promise<any> => {
    try {
        const response = await api.post(`/favorites/toggle/${photoId}`);
        return response.data;
    } catch (error: any) {
        throw new Error(`Failed to toggle favorite: ${error.message}`);
    }
};

export const getFavorites = async (page: number = 0, size: number = 20): Promise<any[]> => {
    try {
        const response = await api.get('/favorites/me', {
            params: { page, size }
        });
        return response.data as any[];
    } catch (error: any) {
        throw new Error(`Failed to get favorites: ${error.message}`);
    }
};

export const checkFavorite = async (photoId: string): Promise<boolean> => {
    try {
        const response = await api.get(`/favorites/check/${photoId}`);
        return response.data as boolean;
    } catch (error: any) {
        throw new Error(`Failed to check favorite: ${error.message}`);
    }
};
