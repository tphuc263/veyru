package share_app.tphucshareapp.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import share_app.tphucshareapp.dto.request.ai.CaptionSuggestionRequest;
import share_app.tphucshareapp.dto.response.ai.CaptionSuggestionResponse;
import share_app.tphucshareapp.dto.response.ai.EngagementAnalysisResponse;
import share_app.tphucshareapp.dto.response.ai.PostTimingSuggestionResponse;
import share_app.tphucshareapp.model.Like;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.repository.LikeRepository;
import share_app.tphucshareapp.repository.PhotoRepository;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AIService implements IAIService {

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini.model:gemini-2.0-flash}")
    private String geminiModel;

    private final PhotoRepository photoRepository;
    private final LikeRepository likeRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AIService(PhotoRepository photoRepository,
                     LikeRepository likeRepository,
                     ObjectMapper objectMapper) {
        this.photoRepository = photoRepository;
        this.likeRepository = likeRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    // ==================== CAPTION SUGGESTION ====================
    @Override
    public CaptionSuggestionResponse suggestCaptions(CaptionSuggestionRequest request) {
        log.info("Generating caption suggestions for: {}", request.getImageDescription());

        String prompt = buildCaptionPrompt(request);
        String aiResponse = callGeminiApi(prompt);

        if (aiResponse == null || aiResponse.isBlank()) {
            return fallbackCaptionResponse(request);
        }

        return parseCaptionResponse(aiResponse, request);
    }

    private String buildCaptionPrompt(CaptionSuggestionRequest request) {
        String lang = request.getLanguage() != null ? request.getLanguage() : "vi";
        String langInstruction = lang.equals("vi")
                ? "Respond in Vietnamese."
                : "Respond in English.";

        StringBuilder sb = new StringBuilder();
        sb.append("You are a social media expert for a photo sharing app like Instagram. ");
        sb.append(langInstruction).append("\n\n");
        sb.append("Generate exactly 3 creative, engaging captions for a photo post.\n");

        if (request.getImageDescription() != null && !request.getImageDescription().isBlank()) {
            sb.append("Photo description: ").append(request.getImageDescription()).append("\n");
        }
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            sb.append("Tags: ").append(String.join(", ", request.getTags())).append("\n");
        }
        if (request.getMood() != null && !request.getMood().isBlank()) {
            sb.append("Mood/Style: ").append(request.getMood()).append("\n");
        }

        sb.append("\nRules:\n");
        sb.append("- Each caption should be 1-3 sentences\n");
        sb.append("- Include relevant emojis\n");
        sb.append("- Make them engaging and suitable for social media\n");
        sb.append("- Also suggest 5 relevant hashtags\n\n");
        sb.append("Format your response EXACTLY like this (no extra text):\n");
        sb.append("CAPTION_1: [first caption]\n");
        sb.append("CAPTION_2: [second caption]\n");
        sb.append("CAPTION_3: [third caption]\n");
        sb.append("TAGS: [tag1, tag2, tag3, tag4, tag5]");

        return sb.toString();
    }

    private CaptionSuggestionResponse parseCaptionResponse(String aiResponse, CaptionSuggestionRequest request) {
        List<String> captions = new ArrayList<>();
        List<String> suggestedTags = new ArrayList<>();

        String[] lines = aiResponse.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("CAPTION_1:") || line.startsWith("CAPTION_2:") || line.startsWith("CAPTION_3:")) {
                String caption = line.substring(line.indexOf(":") + 1).trim();
                if (!caption.isBlank()) {
                    captions.add(caption);
                }
            } else if (line.startsWith("TAGS:")) {
                String tagsStr = line.substring(5).trim()
                        .replace("[", "").replace("]", "");
                suggestedTags = Arrays.stream(tagsStr.split(","))
                        .map(String::trim)
                        .filter(t -> !t.isBlank())
                        .map(t -> t.startsWith("#") ? t.substring(1) : t)
                        .collect(Collectors.toList());
            }
        }

        if (captions.isEmpty()) {
            return fallbackCaptionResponse(request);
        }

        return new CaptionSuggestionResponse(captions, suggestedTags);
    }

    private CaptionSuggestionResponse fallbackCaptionResponse(CaptionSuggestionRequest request) {
        List<String> captions = new ArrayList<>();
        String desc = request.getImageDescription() != null ? request.getImageDescription() : "photo";

        captions.add("✨ " + desc + " vibes ✨");
        captions.add("Khoảnh khắc đáng nhớ 📸 " + desc);
        captions.add("Một ngày tuyệt vời! 🌟 #" + desc.replace(" ", ""));

        List<String> tags = request.getTags() != null ? request.getTags() : List.of("photography", "photooftheday");
        return new CaptionSuggestionResponse(captions, tags);
    }

    // ==================== ENGAGEMENT ANALYSIS ====================
    @Override
    public EngagementAnalysisResponse analyzeEngagement(String userId, int recentPostCount) {
        log.info("Analyzing engagement for user: {}, recentPostCount: {}", userId, recentPostCount);

        int count = recentPostCount > 0 ? Math.min(recentPostCount, 50) : 20;
        List<Photo> photos = photoRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);

        // Take only recent posts
        List<Photo> recentPhotos = photos.stream().limit(count).toList();

        if (recentPhotos.isEmpty()) {
            return new EngagementAnalysisResponse(0, 0, 0, "no_data",
                    List.of(), "Chưa có bài đăng nào để phân tích.");
        }

        // Calculate metrics
        double avgLikes = recentPhotos.stream().mapToLong(Photo::getLikeCount).average().orElse(0);
        double avgComments = recentPhotos.stream().mapToLong(Photo::getCommentCount).average().orElse(0);
        double totalEngagement = recentPhotos.stream()
                .mapToDouble(p -> p.getLikeCount() + p.getCommentCount() * 2.0)
                .sum();
        double engagementRate = recentPhotos.size() > 0 ? totalEngagement / recentPhotos.size() : 0;

        // Determine trend
        String trend = calculateTrend(recentPhotos);

        // Top posts by engagement
        List<EngagementAnalysisResponse.PostInsight> topPosts = recentPhotos.stream()
                .sorted((a, b) -> {
                    double scoreA = a.getLikeCount() + a.getCommentCount() * 2.0;
                    double scoreB = b.getLikeCount() + b.getCommentCount() * 2.0;
                    return Double.compare(scoreB, scoreA);
                })
                .limit(5)
                .map(p -> new EngagementAnalysisResponse.PostInsight(
                        p.getId(),
                        p.getCaption() != null ? (p.getCaption().length() > 80 ? p.getCaption().substring(0, 80) + "..." : p.getCaption()) : "",
                        p.getImageUrl(),
                        p.getLikeCount(),
                        p.getCommentCount(),
                        p.getLikeCount() + p.getCommentCount() * 2.0
                ))
                .toList();

        // Build AI summary
        String aiSummary = buildEngagementSummary(avgLikes, avgComments, engagementRate, trend, recentPhotos, topPosts);

        return new EngagementAnalysisResponse(
                Math.round(avgLikes * 100.0) / 100.0,
                Math.round(avgComments * 100.0) / 100.0,
                Math.round(engagementRate * 100.0) / 100.0,
                trend,
                topPosts,
                aiSummary
        );
    }

    private String calculateTrend(List<Photo> photos) {
        if (photos.size() < 4) return "insufficient_data";

        int half = photos.size() / 2;
        // Photos are ordered most recent first
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

    private String buildEngagementSummary(double avgLikes, double avgComments, double engagementRate,
                                          String trend, List<Photo> photos,
                                          List<EngagementAnalysisResponse.PostInsight> topPosts) {
        // Try AI-generated summary first
        String prompt = buildEngagementAnalysisPrompt(avgLikes, avgComments, engagementRate, trend, photos, topPosts);
        String aiResponse = callGeminiApi(prompt);

        if (aiResponse != null && !aiResponse.isBlank()) {
            return aiResponse.trim();
        }

        // Fallback to template-based summary
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("📊 Trong %d bài đăng gần đây:\n", photos.size()));
        summary.append(String.format("• Trung bình %.1f lượt thích và %.1f bình luận mỗi bài\n", avgLikes, avgComments));
        summary.append(String.format("• Điểm tương tác trung bình: %.1f\n", engagementRate));

        switch (trend) {
            case "growing" -> summary.append("• 📈 Xu hướng: Tăng trưởng tốt! Tiếp tục phát huy nhé!");
            case "declining" -> summary.append("• 📉 Xu hướng: Đang giảm. Hãy thử thay đổi nội dung hoặc thời gian đăng bài.");
            case "stable" -> summary.append("• ➡️ Xu hướng: Ổn định. Có thể thử nội dung mới để tăng tương tác.");
            default -> summary.append("• Cần thêm dữ liệu để phân tích xu hướng.");
        }

        return summary.toString();
    }

    private String buildEngagementAnalysisPrompt(double avgLikes, double avgComments, double engagementRate,
                                                  String trend, List<Photo> photos,
                                                  List<EngagementAnalysisResponse.PostInsight> topPosts) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a social media analytics expert. Respond in Vietnamese. ");
        sb.append("Analyze this Instagram-like account's engagement data and give actionable advice.\n\n");
        sb.append(String.format("Total posts analyzed: %d\n", photos.size()));
        sb.append(String.format("Average likes per post: %.1f\n", avgLikes));
        sb.append(String.format("Average comments per post: %.1f\n", avgComments));
        sb.append(String.format("Engagement rate: %.1f\n", engagementRate));
        sb.append(String.format("Trend: %s\n\n", trend));

        if (!topPosts.isEmpty()) {
            sb.append("Top performing posts:\n");
            for (var post : topPosts) {
                sb.append(String.format("- Caption: \"%s\" | Likes: %d | Comments: %d\n",
                        post.getCaption(), post.getLikeCount(), post.getCommentCount()));
            }
        }

        // Include tag analysis
        Map<String, Long> tagFrequency = new HashMap<>();
        for (Photo p : photos) {
            if (p.getTags() != null) {
                for (String tag : p.getTags()) {
                    tagFrequency.merge(tag, 1L, Long::sum);
                }
            }
        }
        if (!tagFrequency.isEmpty()) {
            sb.append("\nMost used tags: ");
            sb.append(tagFrequency.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .map(e -> "#" + e.getKey() + " (" + e.getValue() + ")")
                    .collect(Collectors.joining(", ")));
            sb.append("\n");
        }

        sb.append("\nProvide a concise analysis (3-5 bullet points) with:\n");
        sb.append("1. Overall performance assessment\n");
        sb.append("2. What type of content performs best\n");
        sb.append("3. Specific, actionable tips to improve engagement\n");
        sb.append("Keep it under 300 words. Use emojis for visual appeal.");

        return sb.toString();
    }

    // ==================== POST TIMING SUGGESTION ====================
    @Override
    public PostTimingSuggestionResponse suggestPostTiming(String userId) {
        log.info("Suggesting post timing for user: {}", userId);

        List<Photo> photos = photoRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);

        if (photos.size() < 3) {
            return getDefaultTimingSuggestion();
        }

        // Analyze when the best engagement happens
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

        // Find best days
        List<PostTimingSuggestionResponse.TimingSlot> bestTimes = new ArrayList<>();

        // Top days with best average engagement
        List<Map.Entry<DayOfWeek, Double>> dayAvgs = engagementByDay.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().stream().mapToDouble(d -> d).average().orElse(0)))
                .sorted(Map.Entry.<DayOfWeek, Double>comparingByValue().reversed())
                .limit(3)
                .toList();

        // Top hours with best average engagement
        List<Map.Entry<Integer, Double>> hourAvgs = engagementByHour.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().stream().mapToDouble(d -> d).average().orElse(0)))
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(3)
                .toList();

        // Combine best days and hours
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

        // Sort by score and take top 5
        bestTimes.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        bestTimes = bestTimes.stream().limit(5).toList();

        // Build AI summary
        String aiSummary = buildTimingSummary(userId, photos, dayAvgs, hourAvgs);

        return new PostTimingSuggestionResponse(bestTimes, aiSummary);
    }

    private PostTimingSuggestionResponse getDefaultTimingSuggestion() {
        List<PostTimingSuggestionResponse.TimingSlot> defaults = List.of(
                new PostTimingSuggestionResponse.TimingSlot("Thứ Hai", "07:00 - 09:00", 8.5,
                        "Khung giờ sáng sớm phổ biến cho Instagram"),
                new PostTimingSuggestionResponse.TimingSlot("Thứ Tư", "12:00 - 13:00", 8.0,
                        "Giờ nghỉ trưa - nhiều người online"),
                new PostTimingSuggestionResponse.TimingSlot("Thứ Sáu", "17:00 - 19:00", 9.0,
                        "Cuối tuần - người dùng thư giãn nhiều hơn"),
                new PostTimingSuggestionResponse.TimingSlot("Thứ Bảy", "10:00 - 11:00", 8.8,
                        "Cuối tuần sáng - thời gian rảnh"),
                new PostTimingSuggestionResponse.TimingSlot("Chủ Nhật", "19:00 - 21:00", 8.2,
                        "Tối Chủ Nhật - chuẩn bị tuần mới")
        );

        return new PostTimingSuggestionResponse(defaults,
                "💡 Bạn chưa có đủ dữ liệu để phân tích cá nhân hóa. " +
                        "Đây là gợi ý dựa trên thống kê chung của mạng xã hội. " +
                        "Hãy đăng thêm bài để nhận phân tích chính xác hơn!");
    }

    private String buildTimingSummary(String userId, List<Photo> photos,
                                      List<Map.Entry<DayOfWeek, Double>> dayAvgs,
                                      List<Map.Entry<Integer, Double>> hourAvgs) {
        String prompt = buildTimingPrompt(photos, dayAvgs, hourAvgs);
        String aiResponse = callGeminiApi(prompt);

        if (aiResponse != null && !aiResponse.isBlank()) {
            return aiResponse.trim();
        }

        // Fallback
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

    private String buildTimingPrompt(List<Photo> photos,
                                     List<Map.Entry<DayOfWeek, Double>> dayAvgs,
                                     List<Map.Entry<Integer, Double>> hourAvgs) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a social media timing expert. Respond in Vietnamese.\n");
        sb.append("Based on this user's posting data, give personalized timing advice.\n\n");
        sb.append(String.format("Total posts: %d\n", photos.size()));

        sb.append("Best days (avg engagement):\n");
        for (var e : dayAvgs) {
            sb.append(String.format("- %s: %.1f\n",
                    e.getKey().getDisplayName(TextStyle.FULL, Locale.ENGLISH), e.getValue()));
        }

        sb.append("Best hours (avg engagement):\n");
        for (var e : hourAvgs) {
            sb.append(String.format("- %02d:00: %.1f\n", e.getKey(), e.getValue()));
        }

        sb.append("\nProvide a concise 2-3 sentence summary about when to post. ");
        sb.append("Be specific with days and times. Use emojis. Keep under 150 words.");

        return sb.toString();
    }

    // ==================== GEMINI API INTEGRATION ====================
    private String callGeminiApi(String prompt) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            log.warn("Gemini API key not configured, using fallback responses");
            return null;
        }

        try {
            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    geminiModel, geminiApiKey
            );

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.8,
                            "maxOutputTokens", 1024
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode content = candidates.get(0).path("content").path("parts");
                    if (content.isArray() && !content.isEmpty()) {
                        return content.get(0).path("text").asText();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to call Gemini API: {}", e.getMessage());
        }

        return null;
    }
}
