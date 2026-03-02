package share_app.tphucshareapp.dto.response.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptionSuggestionResponse {
    private List<String> captions;
    private List<String> suggestedTags;
}
