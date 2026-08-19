package com.veyru.dto.request.message;

import lombok.Data;

@Data
public class SendMessageRequest {
    private String receiverId;
    private String text;
}
