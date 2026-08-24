import api from '../config/ApiConfig';
import {
    EngagementAnalysisRequest,
    EngagementAnalysisResponse,
    PostTimingSuggestionResponse,
    ImageAnalysisRequest,
    ImageAnalysisResponse
} from '../types/api';

export const analyzeEngagement = async (request?: EngagementAnalysisRequest): Promise<EngagementAnalysisResponse> => {
    const response = await api.post<EngagementAnalysisResponse>('/ai/engagement-analyses', request || {});
    return response.data;
};

export const suggestPostTiming = async (): Promise<PostTimingSuggestionResponse> => {
    const response = await api.get<PostTimingSuggestionResponse>('/ai/post-timing-suggestions');
    return response.data;
};

export const analyzeImage = async (request: ImageAnalysisRequest): Promise<ImageAnalysisResponse> => {
    const response = await api.post<ImageAnalysisResponse>('/ai/image-analyses', request);
    return response.data;
};

// Get trending hashtags
export const getTrendingHashtags = async (limit: number = 10): Promise<string[]> => {
    const response = await api.get<string[]>('/tags', { params: { sort: 'trending', limit } });
    return response.data;
};
