package com.veyru.service.favorite;

import com.veyru.dto.response.photo.PhotoResponse;

import java.util.List;

public interface IFavoriteService {

    PhotoResponse toggleFavorite(String photoId);

    List<PhotoResponse> getFavorites(int page, int size);

    boolean isFavorited(String photoId);
}
