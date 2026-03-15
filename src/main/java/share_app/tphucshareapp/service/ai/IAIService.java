package share_app.tphucshareapp.service.ai;

import share_app.tphucshareapp.dto.request.ai.ImageAnalysisRequest;
import share_app.tphucshareapp.dto.response.ai.EngagementAnalysisResponse;
import share_app.tphucshareapp.dto.response.ai.ImageAnalysisResponse;
import share_app.tphucshareapp.dto.response.ai.PostTimingSuggestionResponse;

public interface IAIService {
    EngagementAnalysisResponse analyzeEngagement(String userId, int recentPostCount);

    PostTimingSuggestionResponse suggestPostTiming(String userId);

    ImageAnalysisResponse analyzeImage(ImageAnalysisRequest request);
}
