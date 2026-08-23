package com.veyru.adapter.in.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateCommentRequest(
    @NotBlank(message = "Comment text cannot be blank")
        @Size(max = 500, message = "Comment text cannot exceed 500 characters")
        String text,
    String parentCommentId,
    List<String> mentionedUsernames) {


}
