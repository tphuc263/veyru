package share_app.tphucshareapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import share_app.tphucshareapp.enums.UserRole;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;
    
    @Indexed(unique = true)
    private String username;
    
    @Indexed(unique = true)
    private String email;
    
    @Indexed(unique = true, sparse = true)
    private String phoneNumber;
    
    private String password;
    private UserRole role;
    private String imageUrl;
    private String bio;
    private Instant createdAt;

    private long photoCount;
    private long followerCount;
    private long followingCount;

    private List<String> followingIds;

    private String resetToken;
    private Instant resetTokenExpiry;

    public boolean isResetTokenValid() {
        return resetToken != null
                && resetTokenExpiry != null
                && Instant.now().isBefore(resetTokenExpiry);
    }
}
