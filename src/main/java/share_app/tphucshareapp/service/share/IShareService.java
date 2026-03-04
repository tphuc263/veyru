package share_app.tphucshareapp.service.share;

import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.dto.response.share.ShareResponse;

import java.util.List;

public interface IShareService {

    PhotoResponse sharePhoto(String photoId, String caption);

    List<ShareResponse> getPhotoShares(String photoId);

    long getShareCount(String photoId);

    boolean hasShared(String photoId);
}
