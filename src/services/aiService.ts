import api from '../config/ApiConfig';
import {
    EngagementAnalysisRequest,
    EngagementAnalysisResponse,
    PostTimingSuggestionResponse,
    ImageAnalysisRequest,
    ImageAnalysisResponse
} from '../types/api';

export const analyzeEngagement = async (request?: EngagementAnalysisRequest): Promise<EngagementAnalysisResponse> => {
    const response = await api.post('/ai/analyze-engagement', request || {});
    return response as EngagementAnalysisResponse;
};

export const suggestPostTiming = async (): Promise<PostTimingSuggestionResponse> => {
    const response = await api.get('/ai/suggest-timing');
    return response as PostTimingSuggestionResponse;
};

export const analyzeImage = async (request: ImageAnalysisRequest): Promise<ImageAnalysisResponse> => {
    const response = await api.post('/ai/analyze-image', request);
    return response as ImageAnalysisResponse;
};

// Get trending hashtags
export const getTrendingHashtags = async (limit: number = 10): Promise<string[]> => {
    const response = await api.get<ApiResponse<string[]>>(`/tags/trending?limit=${limit}`);
    return response.data as string[];
};
