import api from '../config/ApiConfig';
import { CursorPageResponse, UnifiedPost } from '../types/api';

export const getNewsfeed = async (cursor?: string, size: number = 20): Promise<CursorPageResponse<UnifiedPost>> => {
        const response = await api.get<CursorPageResponse<UnifiedPost>>('/feed/unified', {
            params: { cursor, size }
        });
        return response.data;
};
