package share_app.tphucshareapp.service.search;

import org.springframework.data.domain.Page;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.dto.response.search.UserSearchResponseSimple;

import java.util.List;

public interface ISearchService {

    Page<UserSearchResponseSimple> searchUsers(String query, int page, int size);

    Page<PhotoResponse> searchPhotos(String query, int page, int size);

    Page<PhotoResponse> searchPhotosByTags(String query, int page, int size);

    List<String> getSearchSuggestions(String query, int limit);
}
