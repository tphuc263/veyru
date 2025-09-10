package share_app.tphucshareapp.service.follow;

import share_app.tphucshareapp.dto.response.follow.FollowResponse;
import share_app.tphucshareapp.dto.response.follow.FollowStatsResponse;

import java.util.List;

public interface IFollowService {
    void follow(String targetUserId);

    void unfollow(String targetUserId);

    List<FollowResponse> getFollowers(String userId, int page, int size);

    List<FollowResponse> getFollowing(String userId, int page, int size);

    boolean isFollowing(String followerId, String followingId);
}
