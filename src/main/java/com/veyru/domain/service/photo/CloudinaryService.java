package com.veyru.domain.service.photo;

import com.cloudinary.Cloudinary;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryService {
  private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);
  private static final String UPLOAD_FOLDER = "veyru";
  private static final String SECURE_URL_KEY = "secure_url";
  private static final String PUBLIC_ID_KEY = "public_id";
  private final Cloudinary cloudinary;

  // Cloudinary's Java API returns a raw Map.
  @SuppressWarnings("unchecked")
  public Map<String, Object> uploadImage(MultipartFile file) {
    validateFile(file);
    log.info(
        "Starting image upload - filename: {}, size: {} bytes",
        file.getOriginalFilename(),
        file.getSize());
    Map<String, Object> uploadParams = createUploadParams();
    try {
      Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), uploadParams);
      log.info(
          "Image uploaded successfully - publicId: {}, url: {}",
          result.get(PUBLIC_ID_KEY),
          result.get(SECURE_URL_KEY));
      return result;
    } catch (IOException e) {
      log.error(
          "Failed to upload image to Cloudinary - filename: {}", file.getOriginalFilename(), e);
      throw new RuntimeException("Failed to upload image to Cloudinary", e);
    }
  }

  // Cloudinary's Java API returns a raw Map.
  @SuppressWarnings("unchecked")
  public Map<String, Object> deleteImage(String publicId) {
    if (publicId == null || publicId.trim().isEmpty()) {
      throw new IllegalArgumentException("Public ID cannot be null or empty");
    }
    log.info("Deleting image from Cloudinary - publicId: {}", publicId);
    try {
      Map<String, Object> result = cloudinary.uploader().destroy(publicId, Map.of());
      log.info("Image deleted successfully - publicId: {}", publicId);
      return result;
    } catch (IOException e) {
      log.error("Failed to delete image from Cloudinary - publicId: {}", publicId, e);
      throw new RuntimeException("Failed to delete image from Cloudinary", e);
    }
  }

  public String extractPublicIdFromUrl(String imageUrl) {
    try {
      if (imageUrl == null || !imageUrl.contains("/upload/")) {
        return null;
      }
      String[] parts = imageUrl.split("/upload/");
      if (parts.length < 2) {
        return null;
      }
      String afterUpload = parts[1];
      // Remove query parameters if present
      if (afterUpload.contains("?")) {
        afterUpload = afterUpload.substring(0, afterUpload.indexOf("?"));
      }
      // Remove file extension (.jpg, .png, etc.)
      int lastDotIndex = afterUpload.lastIndexOf('.');
      if (lastDotIndex > 0) {
        afterUpload = afterUpload.substring(0, lastDotIndex);
      }
      // Cloudinary URLs typically have format: /v1234567890/folder/filename
      // Need to remove version number if present
      if (afterUpload.startsWith("/")) {
        afterUpload = afterUpload.substring(1);
      }
      String[] segments = afterUpload.split("/");
      if (segments.length > 0
          && segments[0].startsWith("v")
          && segments[0].substring(1).matches("\\d+")) {
        // Remove version part
        afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
      }
      return afterUpload;
    } catch (Exception e) {
      log.error("Error extracting publicId from URL: {}", imageUrl, e);
      return null;
    }
  }

  // helper methods
  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File cannot be null or empty");
    }
    if (file.getSize() == 0) {
      throw new IllegalArgumentException("File size cannot be zero");
    }
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new IllegalArgumentException("File must be an image");
    }
  }

  private Map<String, Object> createUploadParams() {
    Map<String, Object> params = new HashMap<>();
    params.put("folder", UPLOAD_FOLDER);
    params.put("resource_type", "auto");
    params.put("unique_filename", true);
    return params;
  }

  public CloudinaryService(final Cloudinary cloudinary) {
    this.cloudinary = cloudinary;
  }
}
