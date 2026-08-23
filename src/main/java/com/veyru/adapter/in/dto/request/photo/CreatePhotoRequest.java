package com.veyru.adapter.in.dto.request.photo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record CreatePhotoRequest(
    @NotNull MultipartFile image,
    @Size(max = 2_200) String caption,
    @Size(max = 30) List<@Size(max = 50) String> tags) {}
