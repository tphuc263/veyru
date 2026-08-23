package com.veyru.application.port.out;

import com.veyru.application.common.ImageFile;

public interface ImageStorage {
  String upload(ImageFile image);

  void deleteByUrl(String imageUrl);
}
