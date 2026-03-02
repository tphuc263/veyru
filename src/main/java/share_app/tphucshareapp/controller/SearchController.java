package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.request.search.SearchRequest;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.dto.response.search.SearchResultResponse;
import share_app.tphucshareapp.dto.response.search.UserSearchResponse;
import share_app.tphucshareapp.dto.response.search.UserSearchResponseSimple;
import share_app.tphucshareapp.model.Tag;
import share_app.tphucshareapp.service.photo.ExploreService;
import share_app.tphucshareapp.service.search.SearchService;
import share_app.tphucshareapp.service.user.UserService;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;
    private final ExploreService exploreService;
    private final UserService userService;

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

    // ── Explore (discover) endpoints ──

    @GetMapping("/explore")
    public ResponseEntity<ApiResponse<Page<PhotoResponse>>> getExploreFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            String userId = userService.getCurrentUser().getId();
            log.info("Fetching explore feed for user: {}", userId);
            Page<PhotoResponse> exploreFeed = exploreService.getExploreFeed(userId, page, size);
            return ResponseEntity.ok(ApiResponse.success(exploreFeed, "Explore feed retrieved successfully"));
        } catch (Exception e) {
            log.error("Error fetching explore feed: ", e);
            throw e;
        }
    }

    @GetMapping("/explore/popular")
    public ResponseEntity<ApiResponse<Page<PhotoResponse>>> getPopularPhotos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching popular photos, page: {}, size: {}", page, size);
        Page<PhotoResponse> popular = exploreService.getPopularPhotos(page, size);
        return ResponseEntity.ok(ApiResponse.success(popular, "Popular photos retrieved successfully"));
    }

    @GetMapping("/explore/tags/{tag}")
    public ResponseEntity<ApiResponse<Page<PhotoResponse>>> getPhotosByTag(
            @PathVariable String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching photos for tag: {}", tag);
        Page<PhotoResponse> photos = exploreService.getPhotosByTag(tag, page, size);
        return ResponseEntity.ok(ApiResponse.success(photos, "Photos retrieved for tag: " + tag));
    }
}