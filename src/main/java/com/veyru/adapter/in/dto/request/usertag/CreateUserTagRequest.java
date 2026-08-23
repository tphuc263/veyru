package com.veyru.adapter.in.dto.request.usertag;

import jakarta.validation.constraints.NotBlank;

public record CreateUserTagRequest(
    @NotBlank(message = "Tagged user ID cannot be blank") String taggedUserId,
    Double positionX,
    Double positionY) {}
