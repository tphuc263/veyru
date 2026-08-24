package com.veyru.application.messaging;

public record SendMessageCommand(
    String conversationId,
    String senderId,
    String receiverId,
    String text,
    String clientMessageId) {}
