package share_app.tphucshareapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "conversations")
@CompoundIndex(name = "participants_idx", def = "{'participantIds': 1}")
public class Conversation {

    @Id
    private String id;

    private List<String> participantIds;

    private String lastMessageText;
    private String lastMessageSenderId;
    private Instant lastMessageAt;

    private Instant createdAt;
    private Instant updatedAt;
}
