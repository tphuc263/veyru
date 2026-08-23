package com.veyru.application.discovery;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.veyru.adapter.out.neo4j.Neo4jGraphAdapter;
import com.veyru.application.port.out.FollowStore;
import com.veyru.application.port.out.LikeStore;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.domain.model.Photo;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GraphSyncServiceTest {
  @Test
  void neo4jFailureDoesNotFailThePrimaryPhotoWrite() {
    Neo4jGraphAdapter graph = mock(Neo4jGraphAdapter.class);
    PhotoStore photos = mock(PhotoStore.class);
    Photo photo = new Photo();
    photo.setId("photo-1");
    photo.setUser(new Photo.EmbeddedUser("user-1", "user"));
    when(photos.findById("photo-1")).thenReturn(Optional.of(photo));
    org.mockito.Mockito.doThrow(new RuntimeException("neo4j unavailable"))
        .when(graph)
        .upsertPhoto(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.anyLong(),
            org.mockito.ArgumentMatchers.anyLong(),
            org.mockito.ArgumentMatchers.anyLong(),
            org.mockito.ArgumentMatchers.any());
    GraphSyncService service =
        new GraphSyncService(
            graph, mock(UserStore.class), photos, mock(FollowStore.class), mock(LikeStore.class));

    assertThatCode(() -> service.syncPhoto("photo-1").join()).doesNotThrowAnyException();
  }
}
