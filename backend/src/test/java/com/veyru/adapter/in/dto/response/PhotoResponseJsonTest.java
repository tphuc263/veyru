package com.veyru.adapter.in.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veyru.adapter.in.dto.response.photo.PhotoResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class PhotoResponseJsonTest {
  @Test
  void preservesPersonalizedFlagNames() throws Exception {
    PhotoResponse response =
        new PhotoResponse(
            "photo",
            "user",
            "alice",
            null,
            "image",
            null,
            Instant.EPOCH,
            0,
            0,
            0,
            false,
            false,
            List.of());

    String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(response);

    assertThat(json).contains("\"isLikedByCurrentUser\":false");
    assertThat(json).contains("\"isSavedByCurrentUser\":false");
  }
}
