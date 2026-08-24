import api from '../config/ApiConfig';
import { PaginatedResponse, Photo, SearchUsersResponse } from '../types/api';

export const searchUsers = async (query: string, page: number = 0, size: number = 20): Promise<SearchUsersResponse> => {
        const response = await api.get<SearchUsersResponse>('/search/users', {
            params: { q: query, page, size }
        });
        return response.data;
};

// ── Explore (discover) endpoints under /search/explore ──

export const getExploreFeed = async (page: number = 0, size: number = 20): Promise<PaginatedResponse<Photo>> => {
        const response = await api.get<PaginatedResponse<Photo>>('/search/explore', {
            params: { page, size }
        });
        return response.data;
};

export const getPopularPhotos = async (page: number = 0, size: number = 20): Promise<PaginatedResponse<Photo>> => {
        const response = await api.get<PaginatedResponse<Photo>>('/search/explore/popular', {
            params: { page, size }
        });
        return response.data;
};

export const getPhotosByTag = async (tag: string, page: number = 0, size: number = 20): Promise<PaginatedResponse<Photo>> => {
        const response = await api.get<PaginatedResponse<Photo>>(`/search/explore/tags/${encodeURIComponent(tag)}`, {
            params: { page, size }
        });
        return response.data;
};
