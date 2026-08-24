import api from '../config/ApiConfig';
import { Photo, RecommendedUser } from '../types/api';

/**
 * Get related/similar photos for a given photo (for Explore page).
 */
export const getRelatedPhotos = async (photoId: string, limit: number = 12): Promise<Photo[]> => {
        const response = await api.get<Photo[]>(`/recommendations/photos/${photoId}/related`, {
            params: { limit }
        });
        return response.data;
};

/**
 * Get suggested users to follow (for Home page sidebar).
 */
export const getSuggestedUsers = async (limit: number = 5): Promise<RecommendedUser[]> => {
        const response = await api.get<RecommendedUser[]>('/recommendations/users/suggested', {
            params: { limit }
        });
        return response.data;
};
