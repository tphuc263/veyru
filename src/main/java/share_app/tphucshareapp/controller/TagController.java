package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.service.TagService;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/tags")
@RequiredArgsConstructor
@Slf4j
public class TagController {

    private final TagService tagService;

    /**
     * Get trending hashtags
     * Returns the most popular hashtags from the last 7 days
     */
    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<String>>> getTrendingHashtags(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/v1/tags/trending?limit={}", limit);
        
        List<String> hashtags = tagService.getTrendingHashtags(limit);
        
        // Add # prefix for frontend display
        List<String> formattedHashtags = hashtags.stream()
                .map(tag -> "#" + tag)
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success(formattedHashtags, "Trending hashtags retrieved"));
    }

    /**
     * Get popular hashtags (alias for trending)
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<String>>> getPopularHashtags(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/v1/tags/popular?limit={}", limit);
        
        List<String> hashtags = tagService.getPopularHashtags(limit);
        
        // Add # prefix for frontend display
        List<String> formattedHashtags = hashtags.stream()
                .map(tag -> "#" + tag)
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success(formattedHashtags, "Popular hashtags retrieved"));
    }
}

