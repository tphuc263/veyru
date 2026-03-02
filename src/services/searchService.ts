import api from '../config/ApiConfig';
import { SearchUsersResponse } from '../types/api';

export const searchUsers = async (query: string, page: number = 0, size: number = 20): Promise<SearchUsersResponse> => {
    try {
        const response = await api.get('/search/users', {
            params: { q: query, page, size }
        });
        return response.data as SearchUsersResponse;
    } catch (error: any) {
        throw new Error(`Failed to search users: ${error.message}`);
    }
};
