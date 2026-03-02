package share_app.tphucshareapp.service.favorite;

import share_app.tphucshareapp.dto.response.photo.PhotoResponse;

import java.util.List;

public interface IFavoriteService {

    PhotoResponse toggleFavorite(String photoId);

    List<PhotoResponse> getFavorites(int page, int size);

    boolean isFavorited(String photoId);
}
