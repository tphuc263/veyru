package com.veyru.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.veyru.dto.request.photo.CreatePhotoRequest;
import com.veyru.dto.response.ApiResponse;
import com.veyru.dto.response.photo.PhotoDetailResponse;
import com.veyru.dto.response.photo.PhotoResponse;
import com.veyru.service.photo.PhotoService;

@RestController
@RequestMapping("${api.prefix}/photos")
@RequiredArgsConstructor
public class PhotoController {
    private final PhotoService photoService;

    // Create a photo
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PhotoResponse>> createPhoto(
            @ModelAttribute CreatePhotoRequest request) {
        PhotoResponse photo = photoService.createPhoto(request);
        return ResponseEntity.ok(ApiResponse.success(photo, "Photo created successfully"));
    }

    // get all photo
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<PhotoResponse>>> getAllPhotos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PhotoResponse> photos = photoService.getAllPhotos(page, size);
        return ResponseEntity.ok(ApiResponse.success(photos, "Photos retrieved successfully"));
    }

    // get photo by user id with pagination
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<PhotoResponse>>> getPhotosByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PhotoResponse> photos = photoService.getPhotosByUserId(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(photos, "User photos retrieved successfully"));
    }

    // get photo detail by id
    @GetMapping("/{photoId}")
    public ResponseEntity<ApiResponse<PhotoDetailResponse>> getPhotoById(
            @PathVariable String photoId) {
        PhotoDetailResponse photo = photoService.getPhotoById(photoId);
        return ResponseEntity.ok(ApiResponse.success(photo, "Photo details retrieved successfully"));
    }

    // delete by id
    @DeleteMapping("/{photoId}")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(@PathVariable String photoId) {
        photoService.deletePhoto(photoId);
        return ResponseEntity.ok(ApiResponse.success(null, "Photo deleted successfully"));
    }
}
