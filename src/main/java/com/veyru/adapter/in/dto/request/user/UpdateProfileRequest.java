package com.veyru.adapter.in.dto.request.user;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Size(min = 3, max = 30) String username, @Size(max = 500) String bio, MultipartFile image) {
  public String getUsername() {
    return username;
  }

  public String getBio() {
    return bio;
  }

  public MultipartFile getImage() {
    return image;
  }
}
