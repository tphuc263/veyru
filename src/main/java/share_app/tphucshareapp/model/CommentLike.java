package share_app.tphucshareapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comment_likes")
@CompoundIndex(name = "comment_user_idx", def = "{'commentId': 1, 'userId': 1}", unique = true)
public class CommentLike {
    @Id
    private String id;
    
    @Indexed
    private String commentId;
    
    private String userId;
    
    private Instant createdAt;
}
