package com.veyru.dto.request.photo;

import java.util.List;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreatePhotoRequest {
  private MultipartFile image;
  private String caption;
  private List<String> tags;
}
