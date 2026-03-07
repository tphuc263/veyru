package share_app.tphucshareapp.service.usertag;

import share_app.tphucshareapp.dto.request.usertag.CreateUserTagRequest;
import share_app.tphucshareapp.dto.response.usertag.UserTagResponse;

import java.util.List;

public interface IUserTagService {
    UserTagResponse tagUserInPhoto(String photoId, CreateUserTagRequest request);
    
    void removeUserTag(String photoId, String taggedUserId);
    
    List<UserTagResponse> getPhotoUserTags(String photoId);
    
    List<UserTagResponse> getPhotosWhereUserIsTagged(String userId);
}
