package share_app.tphucshareapp.dto.request.ai;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CaptionSuggestionRequest {
    private String imageDescription;
    private List<String> tags;
    private String mood;
    private String language;
    // Optional: nếu frontend muốn force một userId cụ thể
    // Nếu null, backend sẽ lấy từ current logged-in user
    private String userId;
}
