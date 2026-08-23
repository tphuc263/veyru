package com.veyru.application.media;

import com.veyru.application.common.ImageFile;
import java.util.List;

public record CreatePhotoCommand(ImageFile image, String caption, List<String> tags) {}
