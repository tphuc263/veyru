package com.veyru.adapter.in.dto.request.share;

public record SharePhotoRequest(String caption) {
  public String getCaption() {
    return caption;
  }
}
