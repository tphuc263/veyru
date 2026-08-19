package com.veyru.service.ai;

import com.veyru.dto.request.ai.ImageAnalysisRequest;
import com.veyru.dto.response.ai.EngagementAnalysisResponse;
import com.veyru.dto.response.ai.ImageAnalysisResponse;
import com.veyru.dto.response.ai.PostTimingSuggestionResponse;

public interface IAIService {
    EngagementAnalysisResponse analyzeEngagement(String userId, int recentPostCount);

    PostTimingSuggestionResponse suggestPostTiming(String userId);

    ImageAnalysisResponse analyzeImage(ImageAnalysisRequest request);
}
