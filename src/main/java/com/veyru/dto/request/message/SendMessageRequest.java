package com.veyru.dto.request.message;

public record SendMessageRequest(String receiverId, String text) {
  public String getReceiverId() {
    return receiverId;
  }

  public String getText() {
    return text;
  }
}
