package share_app.tphucshareapp.dto.response.search;

import lombok.Data;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.dto.response.user.UserProfileResponse;

import java.util.List;

@Data
public class SearchResultResponse {
    private List<UserProfileResponse> users;
    private List<PhotoResponse> photos;
    private long totalUsers;
    private long totalPhotos;
    private String query;
}
