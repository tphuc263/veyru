package com.veyru.application.identity;

import com.veyru.application.common.ImageFile;

public record UpdateProfileCommand(String username, String bio, ImageFile image) {}
