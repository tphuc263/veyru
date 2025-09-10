//package share_app.tphucshareapp.service.scheduler;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import share_app.tphucshareapp.dto.response.user.UserProfileResponse;
//import share_app.tphucshareapp.service.photo.NewsfeedService;
//import share_app.tphucshareapp.service.user.UserService;
//
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class NewsfeedCacheScheduler {
//
//    private final NewsfeedService newsfeedService;
//    private final UserService userService;
//
//    /**
//     * Pre-generate newsfeed cache for active users every 2 hours
//     * This runs during off-peak hours to reduce load
//     */
////    @Scheduled(fixedRate = 7200000, initialDelay = 300000) // 2 hours interval, 5 min initial delay
//    public void preGenerateActiveUserFeeds() {
//        log.info("Starting scheduled newsfeed cache generation for active users");
//
//        try {
//            // Get active users (you can implement logic to identify active users)
//            List<String> activeUserIds = getActiveUserIds();
//
//            log.info("Pre-generating feeds for {} active users", activeUserIds.size());
//
//            // Process in batches to avoid overwhelming the system
//            int batchSize = 50;
//            for (int i = 0; i < activeUserIds.size(); i += batchSize) {
//                int endIndex = Math.min(i + batchSize, activeUserIds.size());
//                List<String> batch = activeUserIds.subList(i, endIndex);
//
//                // Process batch asynchronously
//                CompletableFuture.runAsync(() -> processBatch(batch));
//
//                // Small delay between batches
//                Thread.sleep(1000);
//            }
//
//        } catch (Exception e) {
//            log.error("Error in scheduled newsfeed cache generation", e);
//        }
//    }
//
//    /**
//     * Clean up stale cache entries daily
//     */
////    @Scheduled(cron = "0 2 0 * * ?") // Every day at 2 AM
//    public void cleanupStaleCache() {
//        log.info("Starting scheduled cache cleanup");
//
//        try {
//            // Implementation would depend on your Redis setup
//            // You might want to track cache access times and remove unused entries
//            log.info("Cache cleanup completed");
//
//        } catch (Exception e) {
//            log.error("Error during cache cleanup", e);
//        }
//    }
//
//    private List<String> getActiveUserIds() {
//        // Simple implementation: get all users
//        // In production, you'd want to identify truly active users
//        // based on recent activity, login times, etc.
//
//        try {
//            Page<UserProfileResponse> users = userService.getAllUsers(0, 1000); // Adjust as needed
//            return users.getContent().stream()
//                    .map(UserProfileResponse::getId)
//                    .toList();
//
//        } catch (Exception e) {
//            log.error("Error fetching active user IDs", e);
//            return List.of();
//        }
//    }
//
//    private void processBatch(List<String> userIds) {
//        for (String userId : userIds) {
//            try {
//                newsfeedService.generateNewsfeedCache(userId);
//                Thread.sleep(100); // Small delay between users
//
//            } catch (Exception e) {
//                log.error("Error generating cache for user: {}", userId, e);
//            }
//        }
//    }
//}
