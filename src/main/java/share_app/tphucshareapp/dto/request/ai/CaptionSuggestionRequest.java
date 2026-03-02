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
}
