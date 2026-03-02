import api from '../config/ApiConfig';

export interface Tag {
    id: number;
    name: string;
    count?: number;
}

export const getAllTags = async (): Promise<Tag[]> => {
    try {
        const response = await api.get('/tags/all');
        return (response.data ?? response) as Tag[];
    } catch (error: any) {
        console.error('Failed to fetch tags:', error);
        return [];
    }
};

export const searchTags = async (query: string): Promise<Tag[]> => {
    try {
        const response = await api.get('/tags/search', { params: { query } });
        return (response.data ?? response) as Tag[];
    } catch (error: any) {
        console.error('Failed to search tags:', error);
        return [];
    }
};
