package com.veyru.adapter.in.dto.request.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ImageAnalysisRequest(
    @NotBlank @Size(max = 14_000_000) String imageBase64,
    @NotBlank @Size(max = 100) String mimeType,
    String userId) {
  public String getImageBase64() {
    return imageBase64;
  }

  public String getMimeType() {
    return mimeType;
  }

  public String getUserId() {
    return userId;
  }
}
