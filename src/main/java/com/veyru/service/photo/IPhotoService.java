package com.veyru.service.photo;

import com.veyru.dto.request.photo.CreatePhotoRequest;
import com.veyru.dto.response.photo.PhotoDetailResponse;
import com.veyru.dto.response.photo.PhotoResponse;
import org.springframework.data.domain.Page;

public interface IPhotoService {
  PhotoResponse createPhoto(CreatePhotoRequest request);

  Page<PhotoResponse> getAllPhotos(int page, int size);

  Page<PhotoResponse> getPhotosByUserId(String userId, int page, int size);

  PhotoDetailResponse getPhotoById(String photoId);

  void deletePhoto(String photoId);
}
