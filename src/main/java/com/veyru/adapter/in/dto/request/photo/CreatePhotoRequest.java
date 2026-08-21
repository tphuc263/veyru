package com.veyru.adapter.in.dto.request.photo;

import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record CreatePhotoRequest(
    @NotNull MultipartFile image,
    @Size(max = 2_200) String caption,
    @Size(max = 30) List<@Size(max = 50) String> tags) {
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
