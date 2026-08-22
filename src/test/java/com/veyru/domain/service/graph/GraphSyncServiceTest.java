package com.veyru.domain.service.graph;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.veyru.application.port.out.FollowRepository;
import com.veyru.application.port.out.LikeRepository;
import com.veyru.application.port.out.PhotoRepository;
import com.veyru.application.port.out.UserRepository;
import com.veyru.domain.model.Photo;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GraphSyncServiceTest {
  @Test
  void neo4jFailureDoesNotFailThePrimaryPhotoWrite() {
    Neo4jGraphService graph = mock(Neo4jGraphService.class);
    PhotoRepository photos = mock(PhotoRepository.class);
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
            graph,
            mock(UserRepository.class),
            photos,
            mock(FollowRepository.class),
            mock(LikeRepository.class));

    assertThatCode(() -> service.syncPhoto("photo-1").join()).doesNotThrowAnyException();
  }
}
