package com.veyru.service.search;

import com.veyru.dto.response.photo.PhotoResponse;
import com.veyru.dto.response.search.UserSearchResponseSimple;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ISearchService {

  Page<UserSearchResponseSimple> searchUsers(String query, int page, int size);

  Page<PhotoResponse> searchPhotos(String query, int page, int size);

  Page<PhotoResponse> searchPhotosByTags(String query, int page, int size);

  List<String> getSearchSuggestions(String query, int limit);
}
