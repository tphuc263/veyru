package com.veyru.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
    @NotBlank String conversationId,
    @NotBlank String receiverId,
    @NotBlank @Size(max = 4_000) String text) {}
