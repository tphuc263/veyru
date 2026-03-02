package share_app.tphucshareapp.dto.response.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostTimingSuggestionResponse {
    private List<TimingSlot> bestTimes;
    private String aiSummary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimingSlot {
        private String dayOfWeek;
        private String timeRange;
        private double score;
        private String reason;
    }
}
