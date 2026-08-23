package com.veyru.adapter.in.dto.request.user;

import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record UpdateProfileRequest(
    @Size(min = 3, max = 30) String username, @Size(max = 500) String bio, MultipartFile image) {


}
