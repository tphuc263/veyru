package share_app.tphucshareapp.service.ai;

import share_app.tphucshareapp.dto.request.ai.CaptionSuggestionRequest;
import share_app.tphucshareapp.dto.response.ai.CaptionSuggestionResponse;
import share_app.tphucshareapp.dto.response.ai.EngagementAnalysisResponse;
import share_app.tphucshareapp.dto.response.ai.PostTimingSuggestionResponse;

public interface IAIService {
    CaptionSuggestionResponse suggestCaptions(CaptionSuggestionRequest request);
    EngagementAnalysisResponse analyzeEngagement(String userId, int recentPostCount);
    PostTimingSuggestionResponse suggestPostTiming(String userId);
}
