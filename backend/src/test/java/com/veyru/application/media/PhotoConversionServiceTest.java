package com.veyru.application.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.FavoriteStore;
import com.veyru.application.port.out.LikeStore;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import com.veyru.support.DomainFixtures;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class PhotoConversionServiceTest {
  @Test
  void anonymousPhotoReadUsesFalsePersonalizedFlags() {
    LikeStore likes = mock(LikeStore.class);
    FavoriteStore favorites = mock(FavoriteStore.class);
    PhotoConversionService service =
        new PhotoConversionService(likes, favorites, mock(AvatarCache.class));
    Photo photo = Photo.create("user", "alice", "image", null, List.of(), Instant.EPOCH);

    var result = service.convertToPhotoResponse(photo, PhotoViewer.anonymous());

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
    User actor = DomainFixtures.user("actor");
    when(likes.exists(photo.id(), actor.id())).thenReturn(true);
    when(favorites.exists(actor.id(), photo.id())).thenReturn(true);

    var result = service.convertToPhotoResponse(photo, PhotoViewer.authenticated(actor));

    assertThat(result.isLikedByCurrentUser()).isTrue();
    assertThat(result.isSavedByCurrentUser()).isTrue();
  }
}
