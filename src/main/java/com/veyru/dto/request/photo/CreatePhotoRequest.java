package com.veyru.dto.request.photo;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record CreatePhotoRequest(MultipartFile image, String caption, List<String> tags) {
  public MultipartFile getImage() {
    return image;
  }

  public String getCaption() {
    return caption;
  }

  public List<String> getTags() {
    return tags;
  }
}
