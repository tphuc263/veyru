package com.veyru.dto.request.photo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CreatePhotoRequest {
    private MultipartFile image;
    private String caption;
    private List<String> tags;
}
