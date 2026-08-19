package com.veyru.dto.request.share;

public record SharePhotoRequest(String caption) {
  public String getCaption() {
    return caption;
  }
}
