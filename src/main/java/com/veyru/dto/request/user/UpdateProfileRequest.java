package com.veyru.dto.request.user;

import org.springframework.web.multipart.MultipartFile;

public record UpdateProfileRequest(String username, String bio, MultipartFile image) {
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
