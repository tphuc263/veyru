package com.veyru.adapter.out.cloudinary;

import com.cloudinary.Cloudinary;
import com.veyru.application.common.ImageFile;
import com.veyru.application.port.out.ImageStorage;
import com.veyru.domain.exception.ApiException;
import com.veyru.domain.exception.ErrorCode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CloudinaryImageStorage implements ImageStorage {
  private static final Logger log = LoggerFactory.getLogger(CloudinaryImageStorage.class);
  private static final String UPLOAD_FOLDER = "veyru";
  private static final String SECURE_URL_KEY = "secure_url";
  private static final String PUBLIC_ID_KEY = "public_id";
  private final Cloudinary cloudinary;

  // Cloudinary's Java API returns a raw Map.
  @SuppressWarnings("unchecked")
  public String upload(ImageFile file) {
    validateFile(file);
    log.info(
        "Starting image upload - filename: {}, size: {} bytes",
        file.filename(),
        file.bytes().length);
    Map<String, Object> uploadParams = createUploadParams();
    try {
      Map<String, Object> result = cloudinary.uploader().upload(file.bytes(), uploadParams);
      log.info(
          "Image uploaded successfully - publicId: {}, url: {}",
          result.get(PUBLIC_ID_KEY),
          result.get(SECURE_URL_KEY));
      return (String) result.get(SECURE_URL_KEY);
    } catch (IOException e) {
      throw new ApiException(ErrorCode.EXTERNAL_SERVICE_FAILURE, e);
    }
  }

  // Cloudinary's Java API returns a raw Map.
  @SuppressWarnings("unchecked")
  private Map<String, Object> deleteImage(String publicId) {
    if (publicId == null || publicId.trim().isEmpty()) {
      throw new IllegalArgumentException("Public ID cannot be null or empty");
    }
    log.info("Deleting image from Cloudinary - publicId: {}", publicId);
    try {
      Map<String, Object> result = cloudinary.uploader().destroy(publicId, Map.of());
      log.info("Image deleted successfully - publicId: {}", publicId);
      return result;
    } catch (IOException e) {
      throw new ApiException(ErrorCode.EXTERNAL_SERVICE_FAILURE, e);
    }
  }

  private String extractPublicIdFromUrl(String imageUrl) {

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
  }

  // helper methods
  private void validateFile(ImageFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File cannot be null or empty");
    }
    String contentType = file.contentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new IllegalArgumentException("File must be an image");
    }
  }

  @Override
  public void deleteByUrl(String imageUrl) {
    String publicId = extractPublicIdFromUrl(imageUrl);
    if (publicId != null) deleteImage(publicId);
  }

  private Map<String, Object> createUploadParams() {
    Map<String, Object> params = new HashMap<>();
    params.put("folder", UPLOAD_FOLDER);
    params.put("resource_type", "auto");
    params.put("unique_filename", true);
    return params;
  }

  public CloudinaryImageStorage(final Cloudinary cloudinary) {
    this.cloudinary = cloudinary;
  }
}
