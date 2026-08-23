package com.veyru.application.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.FavoriteStore;
import com.veyru.application.port.out.LikeStore;
import com.veyru.domain.model.Photo;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PhotoConversionServiceTest {
  @Test
  void anonymousPhotoReadUsesFalsePersonalizedFlags() {
    LikeStore likes = mock(LikeStore.class);
    FavoriteStore favorites = mock(FavoriteStore.class);
    PhotoConversionService service =
        new PhotoConversionService(likes, favorites, mock(AvatarCache.class));
    Photo photo = Photo.create("user", "alice", "image", null, List.of(), Instant.EPOCH);

    var result = service.convertToPhotoResponse(photo, Optional.empty());

    assertThat(result.isLikedByCurrentUser()).isFalse();
    assertThat(result.isSavedByCurrentUser()).isFalse();
    verifyNoInteractions(likes, favorites);
  }

  @Test
  void authenticatedPhotoReadUsesPersonalizedFlags() {
    LikeStore likes = mock(LikeStore.class);
    FavoriteStore favorites = mock(FavoriteStore.class);
    PhotoConversionService service =
        new PhotoConversionService(likes, favorites, mock(AvatarCache.class));
    Photo photo = Photo.create("owner", "alice", "image", null, List.of(), Instant.EPOCH);
    User actor =
        new User(
            "actor",
            "bob",
            "bob@example.com",
            null,
            "hash",
            null,
            null,
            null,
            Instant.EPOCH,
            0,
            0,
            0,
            null,
            null);
    when(likes.exists(photo.getId(), actor.getId())).thenReturn(true);
    when(favorites.exists(actor.getId(), photo.getId())).thenReturn(true);

    var result = service.convertToPhotoResponse(photo, Optional.of(actor));

    assertThat(result.isLikedByCurrentUser()).isTrue();
    assertThat(result.isSavedByCurrentUser()).isTrue();
  }
}
