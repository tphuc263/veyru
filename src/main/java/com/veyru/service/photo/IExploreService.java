package com.veyru.service.photo;

import org.springframework.data.domain.Page;
import com.veyru.dto.response.photo.PhotoResponse;

public interface IExploreService {
    /**
     * Get explore feed - trending/popular photos from users you don't follow
     */
    Page<PhotoResponse> getExploreFeed(String userId, int page, int size);

    /**
     * Get popular photos overall (sorted by engagement)
     */
    Page<PhotoResponse> getPopularPhotos(int page, int size);

    /**
     * Get photos by tag for explore
     */
    Page<PhotoResponse> getPhotosByTag(String tag, int page, int size);
}
