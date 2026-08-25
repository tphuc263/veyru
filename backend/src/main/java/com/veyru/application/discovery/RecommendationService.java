package com.veyru.application.discovery;

import com.veyru.application.common.PageQuery;
import com.veyru.application.common.error.UseCaseError;
import com.veyru.application.common.error.UseCaseException;
import com.veyru.application.intelligence.EmbeddingService;
import com.veyru.application.media.PhotoConversionService;
import com.veyru.application.port.out.CurrentActor;
import com.veyru.application.port.out.FollowStore;
import com.veyru.application.port.out.GraphFeedQuery;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.port.out.VectorIndex;
import com.veyru.application.result.photo.PhotoResult;
import com.veyru.application.result.recommendation.RecommendedUserResult;
import com.veyru.domain.model.Follow;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.User;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Graph-based people discovery plus local photo-similarity search. */
public class RecommendationService {
  private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
  private final EmbeddingService embeddingService;
  private final VectorIndex vectorIndex;
  private final GraphFeedQuery graph;
  private final PhotoStore photoStore;
  private final UserStore userStore;
  private final FollowStore followStore;
  private final PhotoConversionService photoConversionService;
  private final CurrentActor currentActor;

  public List<PhotoResult> getRelatedPhotos(String photoId, int limit, Optional<User> currentUser) {
    Photo source = photoStore.findById(photoId).orElse(null);
    if (source == null) return List.of();
    try {
      ensurePhotoEmbedding(source);
      String text = embeddingService.buildPhotoText(source.getCaption(), source.getTags());
      float[] embedding = embeddingService.generateEmbedding(text);
      if (embedding != null) {
        List<String> ids =
            vectorIndex.searchSimilarPhotos(embedding, limit, photoId).stream()
                .map(result -> (String) result.get("entityId"))
                .filter(Objects::nonNull)
                .toList();
        Map<String, Photo> photos =
            photoStore.findAllById(ids).stream()
                .collect(Collectors.toMap(Photo::getId, photo -> photo));
        List<PhotoResult> related =
            ids.stream()
                .map(photos::get)
                .filter(Objects::nonNull)
                .map(photo -> photoConversionService.convertToPhotoResponse(photo, currentUser))
                .toList();
        if (!related.isEmpty()) return related;
      }
    } catch (RuntimeException exception) {
      log.warn("Photo vector search unavailable; using tag fallback", exception);
    }
    return relatedByTags(source, limit, currentUser);
  }

  public List<PhotoResult> getRelatedPhotos(String photoId, int limit) {
    return getRelatedPhotos(photoId, limit, currentActor.id().flatMap(userStore::findById));
  }

  public List<RecommendedUserResult> getSuggestedUsers(int limit) {
    String userId =
        currentActor
            .id()
            .orElseThrow(() -> new UseCaseException(UseCaseError.AUTHENTICATION_REQUIRED));
    return getSuggestedUsers(userId, limit);
  }

  public List<RecommendedUserResult> getSuggestedUsers(String userId, int limit) {
    User currentUser = userStore.findById(userId).orElse(null);
    if (currentUser == null) return List.of();
    Set<String> excluded = followingIds(userId);
    excluded.add(userId);
    List<GraphFeedItem> graphCandidates;
    try {
      graphCandidates = graph.getSuggestedUsers(userId, limit * 2);
    } catch (RuntimeException exception) {
      log.warn("Neo4j suggestions unavailable; using popular-user fallback", exception);
      graphCandidates = List.of();
    }

    List<String> ids =
        graphCandidates.stream()
            .map(GraphFeedItem::id)
            .filter(id -> !excluded.contains(id))
            .distinct()
            .limit(limit)
            .toList();
    Map<String, User> users =
        userStore.findAllById(ids).stream().collect(Collectors.toMap(User::getId, user -> user));
    Map<String, Double> mutualCounts =
        graphCandidates.stream()
            .collect(
                Collectors.toMap(
                    GraphFeedItem::id, GraphFeedItem::score, Math::max, LinkedHashMap::new));
    List<RecommendedUserResult> suggestions = new ArrayList<>();
    for (String id : ids) {
      User candidate = users.get(id);
      if (candidate == null) continue;
      double mutualCount = mutualCounts.getOrDefault(id, 0.0);
      suggestions.add(
          result(
              candidate,
              1.0 - Math.exp(-mutualCount / 3.0),
              "Followed by "
                  + (long) mutualCount
                  + ((long) mutualCount == 1 ? " person" : " people")
                  + " you follow"));
    }

    if (suggestions.size() < limit) {
      excluded.addAll(suggestions.stream().map(RecommendedUserResult::id).toList());
      userStore.findAll().stream()
          .filter(user -> !excluded.contains(user.getId()))
          .sorted(
              Comparator.comparingLong(User::getFollowerCount)
                  .reversed()
                  .thenComparing(User::getId))
          .limit(limit - suggestions.size())
          .map(user -> result(user, 0.0, "Popular on Veyru"))
          .forEach(suggestions::add);
    }
    return List.copyOf(suggestions);
  }

  private RecommendedUserResult result(User user, double score, String reason) {
    return new RecommendedUserResult(
        user.getId(),
        user.getUsername(),
        user.getImageUrl(),
        user.getBio(),
        user.getFollowerCount(),
        user.getPhotoCount(),
        score,
        reason);
  }

  private List<PhotoResult> relatedByTags(Photo source, int limit, Optional<User> currentUser) {
    if (source.getTags() == null || source.getTags().isEmpty()) return List.of();
    return photoStore.findByTags(source.getTags(), new PageQuery(0, limit + 1)).items().stream()
        .filter(photo -> !photo.getId().equals(source.getId()))
        .limit(limit)
        .map(photo -> photoConversionService.convertToPhotoResponse(photo, currentUser))
        .toList();
  }

  private Set<String> followingIds(String userId) {
    return followStore.findByFollowerId(userId).stream()
        .map(Follow::getFollowingId)
        .collect(Collectors.toSet());
  }

  public void ensurePhotoEmbedding(Photo photo) {
    if (vectorIndex.hasPhotoEmbedding(photo.getId())) return;
    String text = embeddingService.buildPhotoText(photo.getCaption(), photo.getTags());
    if (text.isBlank()) return;
    float[] embedding = embeddingService.generateEmbedding(text);
    if (embedding != null) {
      String userId = photo.getUser() == null ? "" : photo.getUser().getUserId();
      vectorIndex.storePhotoEmbedding(
          photo.getId(), embedding, photo.getCaption(), userId, photo.getTags());
    }
  }

  public void indexNewPhoto(String photoId) {
    Photo photo = photoStore.findById(photoId).orElse(null);
    if (photo == null) return;
    try {
      ensurePhotoEmbedding(photo);
    } catch (RuntimeException exception) {
      log.warn("Photo {} will use tag fallback until vector indexing recovers", photoId, exception);
    }
  }

  public int batchIndexAllPhotos() {
    List<Photo> photos = photoStore.findAll();
    photos.forEach(this::ensurePhotoEmbedding);
    return photos.size();
  }

  public RecommendationService(
      EmbeddingService embeddingService,
      VectorIndex vectorIndex,
      GraphFeedQuery graph,
      PhotoStore photoStore,
      UserStore userStore,
      FollowStore followStore,
      PhotoConversionService photoConversionService,
      CurrentActor currentActor) {
    this.embeddingService = embeddingService;
    this.vectorIndex = vectorIndex;
    this.graph = graph;
    this.photoStore = photoStore;
    this.userStore = userStore;
    this.followStore = followStore;
    this.photoConversionService = photoConversionService;
    this.currentActor = currentActor;
  }
}
