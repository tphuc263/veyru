package com.veyru.service.photo;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ICloudinaryService {
    Map<String, Object> uploadImage(MultipartFile file);

    Map<String, Object> deleteImage(String publicId);

    String extractPublicIdFromUrl(String imageUrl);
}
