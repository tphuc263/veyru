package share_app.tphucshareapp.dto.request.ai;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageAnalysisRequest {
    /**
     * Base64 encoded image data
     */
    private String imageBase64;
    
    /**
     * Optional: mime type (image/jpeg, image/png, image/webp)
     */
    private String mimeType;
    
    /**
     * Optional: user context
     */
    private String userId;
}

