package share_app.tphucshareapp.service.photo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.service.user.UserService;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExploreService implements IExploreService {

    private final PhotoRepository photoRepository;
    private final MongoTemplate mongoTemplate;
    private final PhotoConversionService photoConversionService;
    private final UserService userService;

    @Override
    public Page<PhotoResponse> getExploreFeed(String userId, int page, int size) {
        log.info("Fetching explore feed for user: {}, page: {}, size: {}", userId, page, size);

        User currentUser = userService.findUserById(userId);

        // Get user's following list to exclude from explore
        List<String> excludeUserIds = new ArrayList<>();
        excludeUserIds.add(userId); // Exclude own photos
        if (currentUser.getFollowingIds() != null) {
            excludeUserIds.addAll(currentUser.getFollowingIds());
        }

        // Use MongoDB aggregation to get trending photos from non-followed users
        Pageable pageable = PageRequest.of(page, size);

        // Build aggregation pipeline
        Criteria criteria = Criteria.where("user.userId").nin(excludeUserIds);

        // Photos from last 30 days for freshness
        Instant cutoff = Instant.now().minus(Duration.ofDays(30));
        Criteria recentCriteria = criteria.and("createdAt").gte(cutoff);

        AggregationOperation match = Aggregation.match(recentCriteria);

        // Add a computed engagement score field: likeCount * 2 + commentCount * 3
        AggregationOperation addScore = Aggregation.addFields()
                .addFieldWithValue("engagementScore",
                        new org.bson.Document("$add", List.of(
                                new org.bson.Document("$multiply", List.of("$likeCount", 2)),
                                new org.bson.Document("$multiply", List.of("$commentCount", 3))
                        ))
                ).build();

        AggregationOperation sortByScore = Aggregation.sort(Sort.by(Sort.Direction.DESC, "engagementScore", "createdAt"));
        AggregationOperation skip = Aggregation.skip((long) page * size);
        AggregationOperation limit = Aggregation.limit(size);

        Aggregation aggregation = Aggregation.newAggregation(match, addScore, sortByScore, skip, limit);

        List<Photo> photos = mongoTemplate.aggregate(aggregation, "photos", Photo.class).getMappedResults();

        // If not enough recent photos, fallback to all-time popular
        if (photos.isEmpty() && page == 0) {
            log.info("No recent explore photos found, falling back to all-time popular");
            return getPopularPhotos(page, size);
        }

        // Count total matching documents
        Aggregation countAgg = Aggregation.newAggregation(
                Aggregation.match(recentCriteria),
                Aggregation.count().as("total")
        );
        org.bson.Document countResult = mongoTemplate.aggregate(countAgg, "photos", org.bson.Document.class)
                .getUniqueMappedResult();
        long total = countResult != null ? countResult.getInteger("total", 0) : 0;

        List<PhotoResponse> responses = photos.stream()
                .map(photo -> photoConversionService.convertToPhotoResponse(photo, currentUser))
                .toList();

        return new PageImpl<>(responses, pageable, total);
    }

    @Override
    public Page<PhotoResponse> getPopularPhotos(int page, int size) {
        log.info("Fetching popular photos, page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);

        AggregationOperation addScore = Aggregation.addFields()
                .addFieldWithValue("engagementScore",
                        new org.bson.Document("$add", List.of(
                                new org.bson.Document("$multiply", List.of("$likeCount", 2)),
                                new org.bson.Document("$multiply", List.of("$commentCount", 3))
                        ))
                ).build();

        AggregationOperation sortByScore = Aggregation.sort(Sort.by(Sort.Direction.DESC, "engagementScore", "createdAt"));
        AggregationOperation skip = Aggregation.skip((long) page * size);
        AggregationOperation limit = Aggregation.limit(size);

        Aggregation aggregation = Aggregation.newAggregation(addScore, sortByScore, skip, limit);

        List<Photo> photos = mongoTemplate.aggregate(aggregation, "photos", Photo.class).getMappedResults();
        long total = photoRepository.count();

        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
            log.debug("No authenticated user for popular photos");
        }

        User finalCurrentUser = currentUser;
        List<PhotoResponse> responses = photos.stream()
                .map(photo -> photoConversionService.convertToPhotoResponse(photo, finalCurrentUser))
                .toList();

        return new PageImpl<>(responses, pageable, total);
    }

    @Override
    public Page<PhotoResponse> getPhotosByTag(String tag, int page, int size) {
        log.info("Fetching photos by tag: {}, page: {}, size: {}", tag, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Photo> photos = photoRepository.findByTagsIn(List.of(tag.toLowerCase()), pageable);

        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
            log.debug("No authenticated user for tag photos");
        }

        User finalCurrentUser = currentUser;
        List<PhotoResponse> responses = photos.getContent().stream()
                .map(photo -> photoConversionService.convertToPhotoResponse(photo, finalCurrentUser))
                .toList();

        return new PageImpl<>(responses, pageable, photos.getTotalElements());
    }
}
