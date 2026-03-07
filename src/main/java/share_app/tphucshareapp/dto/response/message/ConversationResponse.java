package share_app.tphucshareapp.dto.response.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private String id;
    private String participantId;
    private String participantUsername;
    private String participantImageUrl;
    private String lastMessageText;
    private String lastMessageSenderId;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant lastMessageAt;
    
    private long unreadCount;
}
