package com.veyru.service.share;

import org.springframework.data.domain.Page;
import com.veyru.dto.response.photo.PhotoResponse;
import com.veyru.dto.response.share.ShareResponse;
import com.veyru.dto.response.share.ShareWithPhotoResponse;

import java.util.List;

public interface IShareService {

    PhotoResponse sharePhoto(String photoId, String caption);

    List<ShareResponse> getPhotoShares(String photoId);

    long getShareCount(String photoId);

    boolean hasShared(String photoId);

    Page<ShareWithPhotoResponse> getSharesByUserId(String userId, int page, int size);
}
