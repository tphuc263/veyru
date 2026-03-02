import api from '../config/ApiConfig';
import {
    CaptionSuggestionRequest,
    CaptionSuggestionResponse,
    EngagementAnalysisRequest,
    EngagementAnalysisResponse,
    PostTimingSuggestionResponse
} from '../types/api';

export const suggestCaptions = async (request: CaptionSuggestionRequest): Promise<CaptionSuggestionResponse> => {
    const response = await api.post('/ai/suggest-caption', request);
    return (response.data ?? response) as CaptionSuggestionResponse;
};

export const analyzeEngagement = async (request?: EngagementAnalysisRequest): Promise<EngagementAnalysisResponse> => {
    const response = await api.post('/ai/analyze-engagement', request || {});
    return response.data as EngagementAnalysisResponse;
};

export const suggestPostTiming = async (): Promise<PostTimingSuggestionResponse> => {
    const response = await api.get('/ai/suggest-timing');
    return response.data as PostTimingSuggestionResponse;
};
