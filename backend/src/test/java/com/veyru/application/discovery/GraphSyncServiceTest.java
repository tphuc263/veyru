package com.veyru.application.discovery;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.veyru.application.port.out.FollowStore;
import com.veyru.application.port.out.GraphProjection;
import com.veyru.application.port.out.LikeStore;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class GraphSyncServiceTest {
  @Test
  void neo4jFailureDoesNotFailThePrimaryPhotoWrite() {
    GraphProjection graph = mock(GraphProjection.class);
    PhotoStore photos = mock(PhotoStore.class);
    Photo photo =
        new Photo(
            "photo-1",
            null,
            null,
            Instant.EPOCH,
            List.of(),
            new Photo.EmbeddedUser("user-1", "user"),
            0,
            0,
            0,
            List.of());
    when(photos.findById("photo-1")).thenReturn(Optional.of(photo));
    doThrow(new RuntimeException("neo4j unavailable"))
        .when(graph)
        .upsertPhoto(
            any(), any(), any(), any(), any(), any(), anyLong(), anyLong(), anyLong(), any());
    GraphSyncService service =
        new GraphSyncService(
            graph, mock(UserStore.class), photos, mock(FollowStore.class), mock(LikeStore.class));

    assertThatCode(() -> service.syncPhoto("photo-1").join()).doesNotThrowAnyException();
  }

  @Test
  void syncsPhotoAuthorBeforePhotoProjection() {
    GraphProjection graph = mock(GraphProjection.class);
    UserStore users = mock(UserStore.class);
    PhotoStore photos = mock(PhotoStore.class);
    Photo photo =
        new Photo(
            "photo",
            "https://example.test/photo.png",
            "caption",
            Instant.EPOCH,
            List.of("tag"),
            new Photo.EmbeddedUser("author", "author"),
            0,
            0,
            0,
            List.of());
    User author =
        new User(
            "author",
            "author",
            "author@example.test",
            null,
            "hash",
            null,
            null,
            null,
            Instant.EPOCH,
            1,
            0,
            0,
            null,
            null);
    when(photos.findById("photo")).thenReturn(Optional.of(photo));
    when(users.findById("author")).thenReturn(Optional.of(author));
    var service =
        new GraphSyncService(graph, users, photos, mock(FollowStore.class), mock(LikeStore.class));

    service.syncPhoto("photo");

    InOrder order = inOrder(graph);
    order.verify(graph).upsertUser("author", "author", null, 0, 1, null);
    order
        .verify(graph)
        .upsertPhoto(
            "photo",
            "author",
            "author",
            "https://example.test/photo.png",
            "caption",
            List.of("tag"),
            0,
            0,
            0,
            Instant.EPOCH);
  }
}
