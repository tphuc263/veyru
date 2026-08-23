package com.veyru.application.event;

public record PhotoCreatedEvent(String photoId, String authorId) {}
