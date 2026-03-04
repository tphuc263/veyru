package share_app.tphucshareapp.dto.response.message;

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
    private Instant lastMessageAt;
    private long unreadCount;
}
