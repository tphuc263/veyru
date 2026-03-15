package share_app.tphucshareapp.service.ai;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import share_app.tphucshareapp.dto.request.ai.ImageAnalysisRequest;
import share_app.tphucshareapp.dto.response.ai.EngagementAnalysisResponse;
import share_app.tphucshareapp.dto.response.ai.ImageAnalysisResponse;
import share_app.tphucshareapp.dto.response.ai.PostTimingSuggestionResponse;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.repository.PhotoRepository;

/**
 * Local AI service using algorithmic approaches instead of external APIs.
 */
@Service
@Slf4j
public class AIService implements IAIService {

    private final PhotoRepository photoRepository;

    private static final Map<String, List<String>> SCENE_TAGS = Map.of(
            "nature", List.of("nature", "naturelovers", "beautiful", "landscape", "outdoor", "adventure", "travel"),
            "food", List.of("food", "foodie", "foodporn", "yummy", "delicious", "instafood", "homemade"),
            "portrait", List.of("portrait", "selfie", "model", "beauty", "style", "fashion", "ootd"),
            "city", List.of("city", "urban", "street", "citylife", "architecture", "building", "travel"),
            "beach", List.of("beach", "ocean", "summer", "vacation", "sun", "sea", "travel"),
            "interior", List.of("interior", "design", "home", "decor", "architecture", "room", "living"),
            "general", List.of("photo", "instagood", "photooftheday", "picoftheday", "instadaily"));

    private static final List<String> CAPTION_TEMPLATES = List.of(
            "Beautiful moment ✨", "Living my best life 💫", "Making memories 📸",
            "Just being me 🌟", "Good vibes only ☀️", "Chasing dreams ✨",
            "Life is beautiful 🌈", "Creating memories 🎉");

    public AIService(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    // ==================== USER CONTEXT ====================
    private static class UserContext {
        private boolean hasHistory;
        private double avgCaptionLength;
        private List<String> topTags = new ArrayList<>();

        public boolean isHasHistory() { return hasHistory; }
        public void setHasHistory(boolean hasHistory) { this.hasHistory = hasHistory; }
        public double getAvgCaptionLength() { return avgCaptionLength; }
        public void setAvgCaptionLength(double avgCaptionLength) { this.avgCaptionLength = avgCaptionLength; }
        public List<String> getTopTags() { return topTags; }
        public void setTopTags(List<String> topTags) { this.topTags = topTags; }
    }

    private UserContext buildUserContext(String userId) {
        UserContext context = new UserContext();
        if (userId == null || userId.isBlank()) {
            context.setHasHistory(false);
            return context;
        }

        try {
            List<Photo> userPhotos = photoRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
            List<Photo> recentPhotos = userPhotos.stream().limit(10).toList();

            if (recentPhotos.isEmpty()) {
                context.setHasHistory(false);
                return context;
            }

            context.setHasHistory(true);

            List<String> recentCaptions = recentPhotos.stream()
                    .map(Photo::getCaption)
                    .filter(c -> c != null && !c.isBlank())
                    .toList();

            if (!recentCaptions.isEmpty()) {
                double avgLength = recentCaptions.stream()
                        .mapToInt(String::length)
                        .average().orElse(0);
                context.setAvgCaptionLength(avgLength);
            }

            List<String> topTags = recentPhotos.stream()
                    .filter(p -> p.getTags() != null)
                    .flatMap(p -> p.getTags().stream())
                    .collect(Collectors.groupingBy(t -> t, Collectors.counting()))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .map(Map.Entry::getKey)
                    .toList();
            context.setTopTags(topTags);

        } catch (Exception e) {
            log.warn("Failed to build user context: {}", e.getMessage());
            context.setHasHistory(false);
        }

        return context;
    }

    // ==================== ENGAGEMENT ANALYSIS ====================
    @Override
    public EngagementAnalysisResponse analyzeEngagement(String userId, int recentPostCount) {
        log.info("Analyzing engagement for user: {}, recentPostCount: {}", userId, recentPostCount);

        int count = recentPostCount > 0 ? Math.min(recentPostCount, 50) : 20;
        List<Photo> photos = photoRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
        List<Photo> recentPhotos = photos.stream().limit(count).toList();

        if (recentPhotos.isEmpty()) {
            return new EngagementAnalysisResponse(0, 0, 0, "no_data",
                    List.of(), "Chưa có bài đăng nào để phân tích.");
        }

        double avgLikes = recentPhotos.stream().mapToLong(Photo::getLikeCount).average().orElse(0);
        double avgComments = recentPhotos.stream().mapToLong(Photo::getCommentCount).average().orElse(0);
        double totalEngagement = recentPhotos.stream()
                .mapToDouble(p -> p.getLikeCount() + p.getCommentCount() * 2.0)
                .sum();
        double engagementRate = recentPhotos.size() > 0 ? totalEngagement / recentPhotos.size() : 0;

        String trend = calculateTrend(recentPhotos);

        List<EngagementAnalysisResponse.PostInsight> topPosts = recentPhotos.stream()
                .sorted((a, b) -> Double.compare(
                        b.getLikeCount() + b.getCommentCount() * 2.0,
                        a.getLikeCount() + a.getCommentCount() * 2.0))
                .limit(5)
                .map(p -> new EngagementAnalysisResponse.PostInsight(
                        p.getId(),
                        p.getCaption() != null ? (p.getCaption().length() > 80 ? p.getCaption().substring(0, 80) + "..." : p.getCaption()) : "",
                        p.getImageUrl(),
                        p.getLikeCount(),
                        p.getCommentCount(),
                        p.getLikeCount() + p.getCommentCount() * 2.0))
                .toList();

        String summary = buildEngagementSummary(avgLikes, avgComments, engagementRate, trend, recentPhotos);

        return new EngagementAnalysisResponse(
                Math.round(avgLikes * 100.0) / 100.0,
                Math.round(avgComments * 100.0) / 100.0,
                Math.round(engagementRate * 100.0) / 100.0,
                trend,
                topPosts,
                summary);
    }

    private String calculateTrend(List<Photo> photos) {
        if (photos.size() < 4) return "insufficient_data";

        int half = photos.size() / 2;
        List<Photo> recentHalf = photos.subList(0, half);
        List<Photo> olderHalf = photos.subList(half, photos.size());

        double recentAvg = recentHalf.stream()
                .mapToDouble(p -> p.getLikeCount() + p.getCommentCount() * 2.0)
                .average().orElse(0);
        double olderAvg = olderHalf.stream()
                .mapToDouble(p -> p.getLikeCount() + p.getCommentCount() * 2.0)
                .average().orElse(0);

        if (olderAvg == 0) return "new_account";
        double change = ((recentAvg - olderAvg) / olderAvg) * 100;

        if (change > 15) return "growing";
        if (change < -15) return "declining";
        return "stable";
    }

    private String buildEngagementSummary(double avgLikes, double avgComments,
            double engagementRate, String trend, List<Photo> photos) {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("📊 Trong %d bài đăng gần đây:\n", photos.size()));
        summary.append(String.format("• Trung bình %.1f lượt thích và %.1f bình luận mỗi bài\n", avgLikes, avgComments));
        summary.append(String.format("• Điểm tương tác trung bình: %.1f\n", engagementRate));

        Map<String, Long> tagFrequency = new HashMap<>();
        for (Photo p : photos) {
            if (p.getTags() != null) {
                for (String tag : p.getTags()) {
                    tagFrequency.merge(tag, 1L, Long::sum);
                }
            }
        }

        if (!tagFrequency.isEmpty()) {
            summary.append("\n🏷️ Tags phổ biến nhất: ");
            summary.append(tagFrequency.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .map(e -> "#" + e.getKey())
                    .collect(Collectors.joining(", ")));
            summary.append("\n");
        }

        switch (trend) {
            case "growing" -> summary.append("\n📈 Xu hướng: Tăng trưởng tốt! Tiếp tục phát huy nhé!");
            case "declining" -> summary.append("\n📉 Xu hướng: Đang giảm. Hãy thử thay đổi nội dung hoặc thời gian đăng bài.");
            case "stable" -> summary.append("\n➡️ Xu hướng: Ổn định. Có thể thử nội dung mới để tăng tương tác.");
            case "new_account" -> summary.append("\n🆕 Tài khoản mới! Hãy đăng bài thường xuyên để xây dựng audience.");
            default -> summary.append("\n❓ Cần thêm dữ liệu để phân tích xu hướng.");
        }

        return summary.toString();
    }

    // ==================== POST TIMING ====================
    @Override
    public PostTimingSuggestionResponse suggestPostTiming(String userId) {
        log.info("Suggesting post timing for user: {}", userId);

        List<Photo> photos = photoRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);

        if (photos.size() < 3) {
            return getDefaultTimingSuggestion();
        }

        Map<DayOfWeek, List<Double>> engagementByDay = new EnumMap<>(DayOfWeek.class);
        Map<Integer, List<Double>> engagementByHour = new HashMap<>();

        for (Photo photo : photos) {
            if (photo.getCreatedAt() == null) continue;

            ZonedDateTime postTime = photo.getCreatedAt().atZone(ZoneId.of("Asia/Ho_Chi_Minh"));
            DayOfWeek day = postTime.getDayOfWeek();
            int hour = postTime.getHour();
            double engagement = photo.getLikeCount() + photo.getCommentCount() * 2.0;

            engagementByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(engagement);
            engagementByHour.computeIfAbsent(hour, k -> new ArrayList<>()).add(engagement);
        }

        List<Map.Entry<DayOfWeek, Double>> dayAvgs = engagementByDay.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().stream().mapToDouble(d -> d).average().orElse(0)))
                .sorted(Map.Entry.<DayOfWeek, Double>comparingByValue().reversed())
                .limit(3)
                .toList();

        List<Map.Entry<Integer, Double>> hourAvgs = engagementByHour.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().stream().mapToDouble(d -> d).average().orElse(0)))
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(3)
                .toList();

        List<PostTimingSuggestionResponse.TimingSlot> bestTimes = new ArrayList<>();
        for (var dayEntry : dayAvgs) {
            String dayName = dayEntry.getKey().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("vi"));
            for (var hourEntry : hourAvgs) {
                int hour = hourEntry.getKey();
                String timeRange = String.format("%02d:00 - %02d:00", hour, (hour + 1) % 24);
                double score = (dayEntry.getValue() + hourEntry.getValue()) / 2.0;
                String reason = String.format("Dựa trên phân tích %d bài đăng của bạn", photos.size());
                bestTimes.add(new PostTimingSuggestionResponse.TimingSlot(dayName, timeRange,
                        Math.round(score * 100.0) / 100.0, reason));
            }
        }

        bestTimes.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        bestTimes = bestTimes.stream().limit(5).toList();

        String summary = buildTimingSummary(photos, dayAvgs, hourAvgs);

        return new PostTimingSuggestionResponse(bestTimes, summary);
    }

    private PostTimingSuggestionResponse getDefaultTimingSuggestion() {
        List<PostTimingSuggestionResponse.TimingSlot> defaults = List.of(
                new PostTimingSuggestionResponse.TimingSlot("Thứ Hai", "07:00 - 09:00", 8.5, "Khung giờ sáng sớm phổ biến cho Instagram"),
                new PostTimingSuggestionResponse.TimingSlot("Thứ Tư", "12:00 - 13:00", 8.0, "Giờ nghỉ trưa - nhiều người online"),
                new PostTimingSuggestionResponse.TimingSlot("Thứ Sáu", "17:00 - 19:00", 9.0, "Cuối tuần - người dùng thư giãn nhiều hơn"),
                new PostTimingSuggestionResponse.TimingSlot("Thứ Bảy", "10:00 - 11:00", 8.8, "Cuối tuần sáng - thời gian rảnh"),
                new PostTimingSuggestionResponse.TimingSlot("Chủ Nhật", "19:00 - 21:00", 8.2, "Tối Chủ Nhật - chuẩn bị tuần mới"));

        return new PostTimingSuggestionResponse(defaults,
                "💡 Bạn chưa có đủ dữ liệu để phân tích cá nhân hóa. " +
                        "Đây là gợi ý dựa trên thống kê chung của mạng xã hội. " +
                        "Hãy đăng thêm bài để nhận phân tích chính xác hơn!");
    }

    private String buildTimingSummary(List<Photo> photos,
            List<Map.Entry<DayOfWeek, Double>> dayAvgs,
            List<Map.Entry<Integer, Double>> hourAvgs) {
        StringBuilder sb = new StringBuilder();
        sb.append("⏰ Dựa trên phân tích ").append(photos.size()).append(" bài đăng:\n");

        if (!dayAvgs.isEmpty()) {
            sb.append("• Ngày tốt nhất: ");
            sb.append(dayAvgs.get(0).getKey().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("vi")));
            sb.append("\n");
        }
        if (!hourAvgs.isEmpty()) {
            sb.append("• Khung giờ tốt nhất: ");
            sb.append(String.format("%02d:00", hourAvgs.get(0).getKey()));
            sb.append("\n");
        }
        sb.append("💡 Hãy thử nghiệm đăng bài ở các khung giờ khác nhau để tìm thời điểm phù hợp nhất!");
        return sb.toString();
    }

    // ==================== IMAGE ANALYSIS ====================
    @Override
    public ImageAnalysisResponse analyzeImage(ImageAnalysisRequest request) {
        log.info("Analyzing image for user: {}", request.getUserId());

        UserContext userContext = buildUserContext(request.getUserId());
        return generateImageAnalysis(userContext);
    }

    private ImageAnalysisResponse generateImageAnalysis(UserContext userContext) {
        List<String> userTags = userContext.isHasHistory() ? userContext.getTopTags() : List.of();

        List<String> suggestedTags;
        if (!userTags.isEmpty()) {
            suggestedTags = new ArrayList<>(userTags);
            suggestedTags.addAll(SCENE_TAGS.get("general").subList(0, 3));
        } else {
            suggestedTags = new ArrayList<>(SCENE_TAGS.get("general"));
        }

        if (suggestedTags.size() > 8) {
            suggestedTags = suggestedTags.subList(0, 8);
        }

        List<String> captionSuggestions = generateCaptionSuggestions(userContext);
        String sceneType = determineSceneType(userTags);

        return new ImageAnalysisResponse(
                "Photo",
                sceneType,
                "neutral",
                List.of("vibrant"),
                List.of("photo"),
                suggestedTags,
                captionSuggestions);
    }

    private List<String> generateCaptionSuggestions(UserContext userContext) {
        List<String> suggestions = new ArrayList<>();

        for (String template : CAPTION_TEMPLATES) {
            suggestions.add(template);
            if (suggestions.size() >= 3) break;
        }

        if (userContext.isHasHistory() && userContext.getAvgCaptionLength() > 0) {
            if (userContext.getAvgCaptionLength() < 50) {
                suggestions.add("Short and sweet ✨");
            } else if (userContext.getAvgCaptionLength() > 150) {
                suggestions.add("Sharing a moment worth remembering 📸\n.\n.\n#life #memories #moments");
            }
        }

        return suggestions;
    }

    private String determineSceneType(List<String> userTags) {
        for (String tag : userTags) {
            if (SCENE_TAGS.containsKey(tag.toLowerCase())) {
                return tag.toLowerCase();
            }
        }
        return "general";
    }
}

