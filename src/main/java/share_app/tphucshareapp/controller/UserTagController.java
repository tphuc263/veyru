package share_app.tphucshareapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import share_app.tphucshareapp.dto.request.usertag.CreateUserTagRequest;
import share_app.tphucshareapp.dto.response.ApiResponse;
import share_app.tphucshareapp.dto.response.usertag.UserTagResponse;
import share_app.tphucshareapp.service.usertag.IUserTagService;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/user-tags")
@RequiredArgsConstructor
public class UserTagController {
    
    private final IUserTagService userTagService;

    @PostMapping("/photo/{photoId}")
    public ResponseEntity<ApiResponse<UserTagResponse>> tagUserInPhoto(
            @PathVariable String photoId,
            @Valid @RequestBody CreateUserTagRequest request) {
        UserTagResponse response = userTagService.tagUserInPhoto(photoId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "User tagged successfully"));
    }

    @DeleteMapping("/photo/{photoId}/user/{taggedUserId}")
    public ResponseEntity<ApiResponse<Void>> removeUserTag(
            @PathVariable String photoId,
            @PathVariable String taggedUserId) {
        userTagService.removeUserTag(photoId, taggedUserId);
        return ResponseEntity.ok(ApiResponse.success(null, "User tag removed successfully"));
    }

    @GetMapping("/photo/{photoId}")
    public ResponseEntity<ApiResponse<List<UserTagResponse>>> getPhotoUserTags(@PathVariable String photoId) {
        List<UserTagResponse> tags = userTagService.getPhotoUserTags(photoId);
        return ResponseEntity.ok(ApiResponse.success(tags, "Photo user tags retrieved successfully"));
    }

    @GetMapping("/user/{userId}/tagged-photos")
    public ResponseEntity<ApiResponse<List<UserTagResponse>>> getPhotosWhereUserIsTagged(@PathVariable String userId) {
        List<UserTagResponse> tags = userTagService.getPhotosWhereUserIsTagged(userId);
        return ResponseEntity.ok(ApiResponse.success(tags, "User tagged photos retrieved successfully"));
    }
}
