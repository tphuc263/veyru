package com.veyru.controller;

import com.veyru.dto.request.photo.CreatePhotoRequest;
import com.veyru.dto.response.PageResponse;
import com.veyru.dto.response.photo.PhotoDetailResponse;
import com.veyru.dto.response.photo.PhotoResponse;
import com.veyru.service.photo.PhotoService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/photos")
public class PhotoController {
  private final PhotoService photoService;

  // Create a photo
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<PhotoResponse> createPhoto(@ModelAttribute CreatePhotoRequest request) {
    PhotoResponse photo = photoService.createPhoto(request);
    return ResponseEntity.status(201).body(photo);
  }

  // get all photo
  @GetMapping("/all")
  public ResponseEntity<PageResponse<PhotoResponse>> getAllPhotos(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    Page<PhotoResponse> photos = photoService.getAllPhotos(page, size);
    return ResponseEntity.ok(PageResponse.from(photos));
  }

  // get photo by user id with pagination
  @GetMapping("/user/{userId}")
  public ResponseEntity<PageResponse<PhotoResponse>> getPhotosByUserId(
      @PathVariable String userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<PhotoResponse> photos = photoService.getPhotosByUserId(userId, page, size);
    return ResponseEntity.ok(PageResponse.from(photos));
  }

  // get photo detail by id
  @GetMapping("/{photoId}")
  public ResponseEntity<PhotoDetailResponse> getPhotoById(@PathVariable String photoId) {
    PhotoDetailResponse photo = photoService.getPhotoById(photoId);
    return ResponseEntity.ok(photo);
  }

  // delete by id
  @DeleteMapping("/{photoId}")
  public ResponseEntity<Void> deletePhoto(@PathVariable String photoId) {
    photoService.deletePhoto(photoId);
    return ResponseEntity.noContent().build();
  }

  public PhotoController(final PhotoService photoService) {
    this.photoService = photoService;
  }
}
