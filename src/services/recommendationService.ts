import api from '../config/ApiConfig';
import { Photo, RecommendedUser } from '../types/api';

/**
 * Get related/similar photos for a given photo (for Explore page).
 */
export const getRelatedPhotos = async (photoId: string, limit: number = 12): Promise<Photo[]> => {
    try {
        const response = await api.get(`/recommendations/photos/${photoId}/related`, {
            params: { limit }
        });
        return (response.data ?? response) as Photo[];
    } catch (error: any) {
        console.error('Failed to get related photos:', error);
        return [];
    }
};

/**
 * Get suggested users to follow (for Home page sidebar).
 */
export const getSuggestedUsers = async (limit: number = 5): Promise<RecommendedUser[]> => {
    try {
        const response = await api.get('/recommendations/users/suggested', {
            params: { limit }
        });
        return (response.data ?? response) as RecommendedUser[];
    } catch (error: any) {
        console.error('Failed to get suggested users:', error);
        return [];
    }
};

/**
 * Admin: batch index all embeddings.
 */
export const batchIndexAll = async (): Promise<{ photosIndexed: number; usersIndexed: number }> => {
    const response = await api.post('/recommendations/admin/index-all');
    return (response.data ?? response) as { photosIndexed: number; usersIndexed: number };
};
