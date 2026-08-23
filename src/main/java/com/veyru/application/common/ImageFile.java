package com.veyru.application.common;

public record ImageFile(byte[] bytes, String filename, String contentType) {
  public boolean isEmpty() {
    return bytes == null || bytes.length == 0;
  }
}
