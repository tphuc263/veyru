package share_app.tphucshareapp.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateCommentRequest {
    @NotBlank(message = "Comment text cannot be blank")
    @Size(max = 500, message = "Comment text cannot exceed 500 characters")
    private String text;
    
    // For replying to a comment (nested comments)
    private String parentCommentId;
    
    // Mentioned users (usernames extracted from @mentions in text)
    private List<String> mentionedUsernames;
}
