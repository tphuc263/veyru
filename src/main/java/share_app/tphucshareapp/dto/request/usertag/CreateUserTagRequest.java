package share_app.tphucshareapp.dto.request.usertag;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserTagRequest {
    @NotBlank(message = "Tagged user ID cannot be blank")
    private String taggedUserId;
    
    // Position on photo (percentage 0-100)
    private Double positionX;
    private Double positionY;
}
