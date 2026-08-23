package com.veyru.adapter.in.controller;

import com.veyru.adapter.in.dto.request.photo.CreatePhotoRequest;
import com.veyru.adapter.in.dto.response.PageResponse;
import com.veyru.application.common.ImageFile;
import com.veyru.application.common.PageResult;
import com.veyru.application.media.CreatePhotoCommand;
import com.veyru.application.media.PhotoService;
import com.veyru.application.result.photo.PhotoDetailResponse;
import com.veyru.application.result.photo.PhotoResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("${api.prefix}/photos")
@Validated
public class PhotoController {
  private final PhotoService photoService;

  // Create a photo
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<PhotoResponse> createPhoto(
      @Valid @ModelAttribute CreatePhotoRequest request) throws IOException {
    var file = request.image();
    PhotoResponse photo =
        photoService.createPhoto(
            new CreatePhotoCommand(
                new ImageFile(file.getBytes(), file.getOriginalFilename(), file.getContentType()),
                request.caption(),
                request.tags()));
    return ResponseEntity.created(
            ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(photo.getId())
                .toUri())
        .body(photo);
  }

  // get all photo
  @GetMapping
  public ResponseEntity<PageResponse<PhotoResponse>> getAllPhotos(
      @RequestParam(required = false) String userId,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    PageResult<PhotoResponse> photos =
        userId == null
            ? photoService.getAllPhotos(page, size)
            : photoService.getPhotosByUserId(userId, page, size);
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
