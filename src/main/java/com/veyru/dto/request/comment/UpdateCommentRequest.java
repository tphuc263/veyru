package com.veyru.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCommentRequest {
    @NotBlank(message = "Comment text cannot be blank")
    @Size(max = 500, message = "Comment text cannot exceed 500 characters")
    private String text;
}
