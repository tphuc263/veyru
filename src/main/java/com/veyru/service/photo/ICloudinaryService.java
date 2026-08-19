package com.veyru.service.photo;

import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface ICloudinaryService {
  Map<String, Object> uploadImage(MultipartFile file);

  Map<String, Object> deleteImage(String publicId);

  String extractPublicIdFromUrl(String imageUrl);
}
