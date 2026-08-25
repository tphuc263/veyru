package com.veyru.application.discovery;

import com.veyru.application.common.CursorPageResult;
import com.veyru.application.common.PageQuery;
import com.veyru.application.common.PageResult;
import com.veyru.application.discovery.FeedCursorCodec.FeedCursor;
import com.veyru.application.identity.UserProfileService;
import com.veyru.application.media.PhotoConversionService;
import com.veyru.application.port.out.AffinityCache;
import com.veyru.application.port.out.AvatarCache;
import com.veyru.application.port.out.FavoriteStore;
import com.veyru.application.port.out.FollowStore;
import com.veyru.application.port.out.GraphFeedQuery;
import com.veyru.application.port.out.LikeStore;
import com.veyru.application.port.out.PhotoStore;
import com.veyru.application.port.out.ShareStore;
import com.veyru.application.port.out.UserStore;
import com.veyru.application.result.photo.PhotoResult;
import com.veyru.application.result.post.UnifiedPostResult;
import com.veyru.config.NewsfeedProperties;
import com.veyru.domain.model.Follow;
import com.veyru.domain.model.Photo;
import com.veyru.domain.model.Share;
import com.veyru.domain.model.User;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewsfeedService {
  private static final Logger log = LoggerFactory.getLogger(NewsfeedService.class);
  private static final Comparator<ScoredCandidate> RANKING =
      Comparator.comparingDouble(ScoredCandidate::score)
          .reversed()
          .thenComparing(ScoredCandidate::createdAt, Comparator.reverseOrder())
          .thenComparing(ScoredCandidate::reference);

  private final FollowStore followStore;
  private final PhotoStore photoStore;
  private final ShareStore shareStore;
  private final UserStore userStore;
  private final AffinityCache affinityCache;
  private final GraphFeedQuery graph;
  private final PhotoConversionService photoConversionService;
  private final UserProfileService userService;
  private final AvatarCache avatarCache;
  private final LikeStore likeStore;
  private final FavoriteStore favoriteStore;
  private final FeedCursorCodec cursorCodec;
  private final NewsfeedProperties properties;
  private final MeterRegistry meters;
  private final Clock clock;

  /** Legacy photo-only feed retained for compatibility. */
  public PageResult<PhotoResult> getNewsfeed(String userId, int page, int size) {
    User currentUser = userService.findUserById(userId);
    List<String> authorIds = authorIds(userId);
    List<Photo> photos =
        photoStore.findByUsersAfter(authorIds, clock.instant().minus(Duration.ofDays(30)));
    if (photos.isEmpty()) photos = photoStore.findByUsers(authorIds);
    List<Photo> ranked =
        photos.stream().sorted(Comparator.comparing(Photo::getCreatedAt).reversed()).toList();
    return paginatePhotos(ranked, currentUser, new PageQuery(page, size));
  }

  public PageResult<PhotoResult> getSmartNewsfeed(String userId, int page, int size) {
    return getNewsfeed(userId, page, size);
  }

  public CursorPageResult<UnifiedPostResult> getUnifiedNewsfeed(
      String userId, String cursor, int size) {
    Timer.Sample total = Timer.start(meters);
    try {
      User currentUser = userService.findUserById(userId);
      FeedCursor decoded =
          cursor == null || cursor.isBlank() ? null : cursorCodec.decode(userId, cursor);
      Instant rankedAt = decoded == null ? clock.instant() : decoded.rankedAt();
      List<String> authors = authorIds(userId);

      Timer.Sample retrieval = Timer.start(meters);
      List<FeedCandidate> candidates = loadCandidates(authors, rankedAt);
      retrieval.stop(meters.timer("veyru.feed.candidate.retrieval"));

      Map<String, Double> affinities = loadAffinities(userId, authors);
      Timer.Sample ranking = Timer.start(meters);
      List<ScoredCandidate> scored =
          candidates.stream()
              .map(candidate -> score(candidate, affinities, rankedAt))
              .sorted(RANKING)
              .filter(candidate -> decoded == null || after(candidate, decoded))
              .limit(size + 1L)
              .toList();
      ranking.stop(meters.timer("veyru.feed.ranking"));

      boolean hasMore = scored.size() > size;
      List<ScoredCandidate> page = scored.subList(0, Math.min(size, scored.size()));
      Map<String, User> users =
          userStore
              .findAllById(page.stream().map(ScoredCandidate::authorId).distinct().toList())
              .stream()
              .collect(Collectors.toMap(User::getId, Function.identity()));
      List<UnifiedPostResult> items =
          page.stream().map(item -> toResult(item.candidate(), currentUser, users)).toList();
      String nextCursor =
          hasMore && !page.isEmpty()
              ? cursorCodec.encode(
                  userId,
                  new FeedCursor(
                      rankedAt,
                      page.getLast().score(),
                      page.getLast().createdAt(),
                      page.getLast().reference()))
              : null;
      return new CursorPageResult<>(items, nextCursor, hasMore);
    } finally {
      total.stop(meters.timer("veyru.feed.total"));
    }
  }

  public CursorPageResult<UnifiedPostResult> getUnifiedNewsfeed(String cursor, int size) {
    return getUnifiedNewsfeed(userService.requireCurrentUserId(), cursor, size);
  }

  public PageResult<PhotoResult> getNewsfeed(int page, int size) {
    return getNewsfeed(userService.requireCurrentUserId(), page, size);
  }

  public PageResult<PhotoResult> getSmartNewsfeed(int page, int size) {
    return getSmartNewsfeed(userService.requireCurrentUserId(), page, size);
  }

  private List<String> authorIds(String userId) {
    List<String> ids =
        new ArrayList<>(
            followStore.findByFollowerId(userId).stream().map(Follow::getFollowingId).toList());
    ids.add(userId);
    return ids.stream().distinct().toList();
  }

  private List<FeedCandidate> loadCandidates(List<String> authorIds, Instant rankedAt) {
    int limit = properties.ranking().candidateLimit();
    Instant recentCutoff = rankedAt.minus(Duration.ofDays(properties.ranking().lookbackDays()));
    List<Photo> photos = photoStore.findByUsersBetween(authorIds, recentCutoff, rankedAt, limit);
    List<Share> shares = shareStore.findByUsersBetween(authorIds, recentCutoff, rankedAt, limit);
    List<FeedCandidate> recent = merge(photos, shares).stream().limit(limit).toList();
    if (recent.size() >= limit) return recent;

    int remaining = limit - recent.size();
    List<Photo> olderPhotos = photoStore.findByUsersBefore(authorIds, recentCutoff, remaining);
    List<Share> olderShares = shareStore.findByUsersBefore(authorIds, recentCutoff, remaining);
    Map<String, FeedCandidate> combined =
        recent.stream()
            .collect(
                Collectors.toMap(
                    FeedCandidate::reference,
                    Function.identity(),
                    (left, right) -> left,
                    LinkedHashMap::new));
    merge(olderPhotos, olderShares)
        .forEach(candidate -> combined.putIfAbsent(candidate.reference(), candidate));
    return combined.values().stream()
        .sorted(Comparator.comparing(FeedCandidate::createdAt).reversed())
        .limit(limit)
        .toList();
  }

  private List<FeedCandidate> merge(List<Photo> photos, List<Share> shares) {
    List<String> originalIds = shares.stream().map(Share::getPhotoId).distinct().toList();
    Map<String, Photo> originals =
        originalIds.isEmpty()
            ? Map.of()
            : photoStore.findAllById(originalIds).stream()
                .collect(Collectors.toMap(Photo::getId, Function.identity()));
    List<FeedCandidate> candidates = new ArrayList<>();
    photos.stream()
        .filter(photo -> photo.getUser() != null)
        .map(
            photo ->
                new FeedCandidate(
                    "PHOTO:" + photo.getId(),
                    photo.getUser().getUserId(),
                    photo.getCreatedAt(),
                    photo,
                    null))
        .forEach(candidates::add);
    shares.stream()
        .filter(share -> originals.containsKey(share.getPhotoId()))
        .map(
            share ->
                new FeedCandidate(
                    "SHARE:" + share.getId(),
                    share.getUserId(),
                    share.getCreatedAt(),
                    originals.get(share.getPhotoId()),
                    share))
        .forEach(candidates::add);
    return candidates.stream()
        .sorted(Comparator.comparing(FeedCandidate::createdAt).reversed())
        .toList();
  }

  private Map<String, Double> loadAffinities(String viewerId, List<String> authorIds) {
    try {
      var cached = affinityCache.get(viewerId);
      if (cached.isPresent()) {
        meters.counter("veyru.feed.affinity.cache", "result", "hit").increment();
        return cached.get();
      }
      meters.counter("veyru.feed.affinity.cache", "result", "miss").increment();
    } catch (RuntimeException exception) {
      meters.counter("veyru.feed.affinity.cache", "result", "error").increment();
      log.warn("Redis affinity cache unavailable for viewer {}", viewerId, exception);
    }

    try {
      Map<String, Double> affinities =
          graph.getAuthorAffinities(viewerId, authorIds).stream()
              .collect(
                  Collectors.toMap(
                      GraphAffinity::authorId, affinity -> affinityScore(viewerId, affinity)));
      affinities = new LinkedHashMap<>(affinities);
      affinities.put(viewerId, 1.0);
      Map<String, Double> result = Map.copyOf(affinities);
      meters.counter("veyru.feed.graph", "result", "success").increment();
      try {
        affinityCache.put(viewerId, result, properties.cache().affinityTtl());
      } catch (RuntimeException exception) {
        meters.counter("veyru.feed.affinity.cache", "result", "error").increment();
        log.warn("Could not cache affinity for viewer {}", viewerId, exception);
      }
      return result;
    } catch (RuntimeException exception) {
      meters.counter("veyru.feed.graph", "result", "fallback").increment();
      log.warn("Neo4j affinity unavailable; using post-only ranking", exception);
      return Map.of();
    }
  }

  private double affinityScore(String viewerId, GraphAffinity affinity) {
    if (viewerId.equals(affinity.authorId())) return 1.0;
    var ranking = properties.ranking();
    double direct = affinity.followed() ? ranking.directFollowWeight() : 0.0;
    double interactions = 1.0 - Math.exp(-affinity.interactionCount() / 3.0);
    double mutuals = 1.0 - Math.exp(-affinity.mutualCount() / 3.0);
    return direct + ranking.interactionWeight() * interactions + ranking.mutualWeight() * mutuals;
  }

  private ScoredCandidate score(
      FeedCandidate candidate, Map<String, Double> affinities, Instant rankedAt) {
    Photo photo = candidate.photo();
    var ranking = properties.ranking();
    double ageHours =
        Math.max(0.0, Duration.between(candidate.createdAt(), rankedAt).toMillis() / 3_600_000.0);
    double recency = Math.exp(-ageHours / ranking.recencyDecayHours());
    double engagementRaw =
        photo.getLikeCount() * 2.0 + photo.getCommentCount() * 3.0 + photo.getShareCount() * 4.0;
    double engagement = 1.0 - Math.exp(-engagementRaw / ranking.engagementScale());
    String caption = photo.getCaption();
    double quality = caption != null && !caption.isBlank() ? 0.5 : 0.0;
    if (photo.getTags() != null && !photo.getTags().isEmpty()) quality += 0.5;
    double postScore =
        ranking.recencyWeight() * recency
            + ranking.engagementWeight() * engagement
            + ranking.qualityWeight() * quality;
    Double affinity = affinities.get(candidate.authorId());
    double score =
        affinity == null
            ? postScore
            : ranking.graphWeight() * affinity + (1.0 - ranking.graphWeight()) * postScore;
    return new ScoredCandidate(candidate, score);
  }

  private boolean after(ScoredCandidate candidate, FeedCursor cursor) {
    int score = Double.compare(candidate.score(), cursor.lastScore());
    if (score != 0) return score < 0;
    int createdAt = candidate.createdAt().compareTo(cursor.lastCreatedAt());
    if (createdAt != 0) return createdAt < 0;
    return candidate.reference().compareTo(cursor.lastReference()) > 0;
  }

  private UnifiedPostResult toResult(
      FeedCandidate candidate, User currentUser, Map<String, User> users) {
    Photo photo = candidate.photo();
    UnifiedPostResult result = new UnifiedPostResult();
    result.setCreatedAt(candidate.createdAt());
    result.setUserId(candidate.authorId());
    User author = users.get(candidate.authorId());
    if (author != null) {
      result.setUsername(author.getUsername());
      result.setUserImageUrl(avatarCache.getAvatar(author.getId()));
    }
    if (candidate.share() == null) {
      result.setId(photo.getId());
      result.setType(UnifiedPostResult.PostType.PHOTO);
      result.setImageUrl(photo.getImageUrl());
      result.setCaption(photo.getCaption());
      result.setLikeCount((int) photo.getLikeCount());
      result.setCommentCount((int) photo.getCommentCount());
      result.setShareCount((int) photo.getShareCount());
      result.setLikedByCurrentUser(likeStore.exists(photo.getId(), currentUser.getId()));
      result.setSavedByCurrentUser(favoriteStore.exists(currentUser.getId(), photo.getId()));
      return result;
    }

    Share share = candidate.share();
    result.setId("share_" + share.getId());
    result.setType(UnifiedPostResult.PostType.SHARE);
    result.setShareCaption(share.getCaption());
    result.setOriginalPhotoId(photo.getId());
    result.setOriginalImageUrl(photo.getImageUrl());
    result.setOriginalCaption(photo.getCaption());
    result.setOriginalCreatedAt(photo.getCreatedAt());
    result.setOriginalLikeCount((int) photo.getLikeCount());
    result.setOriginalCommentCount((int) photo.getCommentCount());
    result.setOriginalShareCount((int) photo.getShareCount());
    if (photo.getUser() != null) {
      result.setOriginalUsername(photo.getUser().getUsername());
      result.setOriginalUserImageUrl(avatarCache.getAvatar(photo.getUser().getUserId()));
    }
    result.setLikedByCurrentUser(likeStore.exists(photo.getId(), currentUser.getId()));
    result.setSavedByCurrentUser(favoriteStore.exists(currentUser.getId(), photo.getId()));
    return result;
  }

  private PageResult<PhotoResult> paginatePhotos(
      List<Photo> photos, User currentUser, PageQuery page) {
    int start = page.page() * page.size();
    int end = Math.min(start + page.size(), photos.size());
    List<PhotoResult> items =
        start >= photos.size()
            ? List.of()
            : photos.subList(start, end).stream()
                .map(
                    photo ->
                        photoConversionService.convertToPhotoResponse(
                            photo, java.util.Optional.of(currentUser)))
                .toList();
    return new PageResult<>(
        items,
        page.page(),
        page.size(),
        photos.size(),
        (int) Math.ceil((double) photos.size() / page.size()));
  }

  public NewsfeedService(
      FollowStore followStore,
      PhotoStore photoStore,
      ShareStore shareStore,
      UserStore userStore,
      AffinityCache affinityCache,
      GraphFeedQuery graph,
      PhotoConversionService photoConversionService,
      UserProfileService userService,
      AvatarCache avatarCache,
      LikeStore likeStore,
      FavoriteStore favoriteStore,
      FeedCursorCodec cursorCodec,
      NewsfeedProperties properties,
      MeterRegistry meters,
      Clock clock) {
    this.followStore = followStore;
    this.photoStore = photoStore;
    this.shareStore = shareStore;
    this.userStore = userStore;
    this.affinityCache = affinityCache;
    this.graph = graph;
    this.photoConversionService = photoConversionService;
    this.userService = userService;
    this.avatarCache = avatarCache;
    this.likeStore = likeStore;
    this.favoriteStore = favoriteStore;
    this.cursorCodec = cursorCodec;
    this.properties = properties;
    this.meters = meters;
    this.clock = clock;
  }

  private record FeedCandidate(
      String reference, String authorId, Instant createdAt, Photo photo, Share share) {}

  private record ScoredCandidate(FeedCandidate candidate, double score) {
    String reference() {
      return candidate.reference();
    }

    String authorId() {
      return candidate.authorId();
    }

    Instant createdAt() {
      return candidate.createdAt();
    }
  }
}
