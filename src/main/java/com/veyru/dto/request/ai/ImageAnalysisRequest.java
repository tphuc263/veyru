package com.veyru.dto.request.ai;

public record ImageAnalysisRequest(String imageBase64, String mimeType, String userId) {
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
