package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import share_app.tphucshareapp.dto.request.search.SearchRequest;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.dto.response.search.SearchResultResponse;
import share_app.tphucshareapp.dto.response.search.UserSearchResponse;
import share_app.tphucshareapp.dto.response.search.UserSearchResponseSimple;
import share_app.tphucshareapp.model.Tag;
import share_app.tphucshareapp.service.search.SearchService;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserSearchResponseSimple>>> searchUsers(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<UserSearchResponseSimple> users = searchService.searchUsers(query, page, size);
        return ResponseEntity.ok(
                ApiResponse.success(users, "User search completed successfully")
        );
    }

    @GetMapping("/photos")
    public ResponseEntity<ApiResponse<Page<PhotoResponse>>> searchPhotos(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<PhotoResponse> photos = searchService.searchPhotos(query, page, size);
        return ResponseEntity.ok(
                ApiResponse.success(photos, "Photo search completed successfully")
        );
    }

    @GetMapping("/photos/tags")
    public ResponseEntity<ApiResponse<Page<PhotoResponse>>> searchPhotosByTags(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<PhotoResponse> photos = searchService.searchPhotosByTags(query, page, size);
        return ResponseEntity.ok(
                ApiResponse.success(photos, "Photo tag search completed successfully")
        );
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<Tag>>> searchTags(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "10") int limit) {

        List<Tag> tags = searchService.searchTags(query, limit);
        return ResponseEntity.ok(
                ApiResponse.success(tags, "Tag search completed successfully")
        );
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<String>>> getSearchSuggestions(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "10") int limit) {

        List<String> suggestions = searchService.getSearchSuggestions(query, limit);
        return ResponseEntity.ok(
                ApiResponse.success(suggestions, "Search suggestions retrieved successfully")
        );
    }
}