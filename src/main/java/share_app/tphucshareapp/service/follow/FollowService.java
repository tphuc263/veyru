package share_app.tphucshareapp.service.follow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.response.follow.FollowResponse;
import share_app.tphucshareapp.dto.response.follow.FollowStatsResponse;
import share_app.tphucshareapp.model.Follow;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.FollowRepository;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.security.userdetails.AppUserDetails;
import share_app.tphucshareapp.service.photo.NewsfeedService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService implements IFollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final MongoTemplate mongoTemplate;

    @Override
    public void follow(String targetUserId) {
        User currentUser = getCurrentUser();
        Follow existingFollow = checkBeforeFollow(targetUserId, currentUser);

        if (existingFollow != null) {
            throw new RuntimeException("You are already following this user");
        }

        Follow follow = new Follow();
        follow.setFollowerId(currentUser.getId());
        follow.setFollowingId(targetUserId);
        follow.setCreatedAt(Instant.now());

        followRepository.save(follow);
        log.info("User {} followed user {}", currentUser.getId(), targetUserId);

        // increase following count of person click follow
        Query followerQuery = new Query(Criteria.where("_id").is(currentUser.getId()));
        Update followerUpdate = new Update().inc("followingCount", 1).push("followingIds", targetUserId);
        mongoTemplate.updateFirst(followerQuery, followerUpdate, User.class);

        // increase follower of person who have new follower
        Query followingQuery = new Query(Criteria.where("_id").is(targetUserId));
        Update followingUpdate = new Update().inc("followerCount", 1);
        mongoTemplate.updateFirst(followingQuery, followingUpdate, User.class);

        log.info("User {} followed user {}", currentUser.getId(), targetUserId);

//        try {
//            newsfeedService.generateNewsfeedCache(currentUser.getId());
//            log.info("Regenerated newsfeed cache after follow for user: {}", currentUser.getId());
//        } catch (Exception e) {
//            log.error("Error regenerating newsfeed cache after follow", e);
//        }
    }

    @Override
    public void unfollow(String targetUserId) {
        User currentUser = getCurrentUser();
        Follow existingFollow = checkBeforeFollow(targetUserId, currentUser);

        if (existingFollow == null) {
            throw new RuntimeException("You are not following this user");
        }

        followRepository.delete(existingFollow);
        log.info("User {} unfollowed user {}", currentUser.getId(), targetUserId);

        Query followerQuery = new Query(Criteria.where("_id").is(currentUser.getId()));
        Update followerUpdate = new Update().inc("followingCount", -1).pull("followingIds", targetUserId);
        mongoTemplate.updateFirst(followerQuery, followerUpdate, User.class);

        Query followingQuery = new Query(Criteria.where("_id").is(targetUserId));
        Update followingUpdate = new Update().inc("followerCount", -1);
        mongoTemplate.updateFirst(followingQuery, followingUpdate, User.class);

//        try {
//            newsfeedService.generateNewsfeedCache(currentUser.getId());
//            log.info("Regenerated newsfeed cache after unfollow for user: {}", currentUser.getId());
//        } catch (Exception e) {
//            log.error("Error regenerating newsfeed cache after unfollow", e);
//        }
    }


    @Override
    public List<FollowResponse> getFollowers(String userId, int page, int size) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Pageable pageable = PageRequest.of(page, size);
        Page<Follow> follows = followRepository.findByFollowingIdOrderByCreatedAtDesc(userId, pageable);

        List<String> followerIds = follows.getContent().stream()
                .map(Follow::getFollowerId)
                .toList();

        return convertToFollowResponses(followerIds, true);
    }

    @Override
    public List<FollowResponse> getFollowing(String userId, int page, int size) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Pageable pageable = PageRequest.of(page, size);
        Page<Follow> follows = followRepository.findByFollowerIdOrderByCreatedAtDesc(userId, pageable);

        List<String> followingIds = follows.getContent().stream()
                .map(Follow::getFollowingId)
                .toList();

        return convertToFollowResponses(followingIds, false);
    }

    @Override
    public boolean isFollowing(String followerId, String followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    // Helper methods
    private List<FollowResponse> convertToFollowResponses(List<String> userIds, boolean isFollowersList) {
        if (userIds.isEmpty()) {
            return List.of();
        }

        // Fetch users in batch
        Map<String, User> usersMap = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // Get current user's following list for follow status
        Set<String> currentUserFollowing = getCurrentUserFollowing();

        return userIds.stream()
                .map(userId -> {
                    User user = usersMap.get(userId);
                    if (user != null) {
                        FollowResponse response = modelMapper.map(user, FollowResponse.class);
                        response.setUserId(user.getId());
                        response.setFollowedByCurrentUser(currentUserFollowing.contains(userId));
                        return response;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private Set<String> getCurrentUserFollowing() {
        try {
            User currentUser = getCurrentUser();
            return followRepository.findByFollowerId(currentUser.getId())
                    .stream()
                    .map(Follow::getFollowingId)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            // User not authenticated
            return Set.of();
        }
    }

    private Follow checkBeforeFollow(String targetUserId, User currentUser) {
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + targetUserId));

        // Prevent self-following
        if (currentUser.getId().equals(targetUserId)) {
            throw new RuntimeException("You cannot follow yourself");
        }

        return followRepository.findByFollowerIdAndFollowingId(
                currentUser.getId(), targetUserId).orElse(null);
    }

    // helper methods
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof AppUserDetails userDetails)) {
            throw new RuntimeException("User not authenticated properly");
        }
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userDetails.getId());
                    return new RuntimeException("User not found with ID: " + userDetails.getId());
                });
    }
}
