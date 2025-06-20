package share_app.tphucshareapp.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.request.search.SearchRequest;
import share_app.tphucshareapp.dto.response.photo.PhotoResponse;
import share_app.tphucshareapp.dto.response.search.SearchResultResponse;
import share_app.tphucshareapp.dto.response.search.UserSearchResponse;
import share_app.tphucshareapp.dto.response.search.UserSearchResponseSimple;
import share_app.tphucshareapp.dto.response.user.UserProfileResponse;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.Tag;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.repository.TagRepository;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.service.follow.FollowService;
import share_app.tphucshareapp.service.photo.PhotoConversionService;
import share_app.tphucshareapp.service.user.UserService;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService implements ISearchService {

    private final UserRepository userRepository;
    private final PhotoRepository photoRepository;
    private final TagRepository tagRepository;
    private final ModelMapper modelMapper;
    private final PhotoConversionService photoConversionService;
    private final UserService userService;
    private final FollowService followService;

    @Override
    public Page<UserSearchResponseSimple> searchUsers(String query, int page, int size) {
        log.info("Searching users for: {}", query);

        String sanitizedQuery = sanitizeSearchQuery(query);
        if (sanitizedQuery.isEmpty()) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.findByUsernameRegex(sanitizedQuery, pageable);

        return users.map(user -> modelMapper.map(user, UserSearchResponseSimple.class));
    }

    @Override
    public Page<PhotoResponse> searchPhotos(String query, int page, int size) {
        log.info("Searching photos for: {}", query);

        String sanitizedQuery = sanitizeSearchQuery(query);
        if (sanitizedQuery.isEmpty()) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Try text search first
        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
            log.trace("No authenticated user found for photo search.");
        }
        final User finalCurrentUser = currentUser;

        // Try text search first
        try {
            Page<Photo> photos = photoRepository.findByTextSearch(sanitizedQuery, pageable);
            if (!photos.isEmpty()) {
                // FIX: Pass the current user to the conversion method.
                return photos.map(photo -> photoConversionService.convertToPhotoResponse(photo, finalCurrentUser));
            }
        } catch (Exception e) {
            log.warn("Photo text search failed, falling back to regex search: {}", e.getMessage());
        }

        // Fallback to caption search
        Page<Photo> photos = photoRepository.findByCaptionContainingIgnoreCase(sanitizedQuery, pageable);
        return photos.map(photo -> photoConversionService.convertToPhotoResponse(photo, finalCurrentUser));
    }

    @Override
    public Page<PhotoResponse> searchPhotosByTags(String query, int page, int size) {
        log.info("Searching photos by tags for: {}", query);

        String sanitizedQuery = sanitizeSearchQuery(query);
        if (sanitizedQuery.isEmpty()) {
            return Page.empty();
        }

        // 1. Find tags that match the query
        // Assuming the query is a space-separated string of tags, e.g., "nature sky"
        List<String> tagNames = List.of(sanitizedQuery.split("\\s+"));

        if (tagNames.isEmpty()) {
            return Page.empty();
        }

        // 2. Find Photos that contain these tags directly
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
            log.trace("No authenticated user found for photo search by tags.");
        }
        final User finalCurrentUser = currentUser;

        Page<Photo> photos = photoRepository.findByTagsIn(tagNames, pageable);

        return photos.map(photo -> photoConversionService.convertToPhotoResponse(photo, finalCurrentUser));
    }

    @Override
    public List<Tag> searchTags(String query, int limit) {
        log.info("Searching tags for: {}", query);

        String sanitizedQuery = sanitizeSearchQuery(query);
        if (sanitizedQuery.isEmpty()) {
            return List.of();
        }

        List<Tag> tags = tagRepository.findByNameContainingIgnoreCase(sanitizedQuery);
        return tags.stream()
                .limit(limit)
                .toList();
    }

    @Override
    public List<String> getSearchSuggestions(String query, int limit) {
        log.info("Getting search suggestions for: {}", query);

        String sanitizedQuery = sanitizeSearchQuery(query);
        if (sanitizedQuery.isEmpty()) {
            return List.of();
        }

        Set<String> suggestions = new java.util.HashSet<>();

        // Get user suggestions
        try {
            List<User> users = userRepository.findByNameFields(sanitizedQuery,
                    PageRequest.of(0, limit / 3)).getContent();
            users.forEach(user -> {
                suggestions.add(user.getUsername());
            });
        } catch (Exception e) {
            log.warn("Error getting user suggestions: {}", e.getMessage());
        }

        // Get tag suggestions
        try {
            List<Tag> tags = tagRepository.findByNameContainingIgnoreCase(sanitizedQuery);
            tags.stream()
                    .limit(limit / 3)
                    .forEach(tag -> suggestions.add(tag.getName()));
        } catch (Exception e) {
            log.warn("Error getting tag suggestions: {}", e.getMessage());
        }

        return suggestions.stream()
                .limit(limit)
                .sorted()
                .toList();
    }

    // Helper methods
    private String sanitizeSearchQuery(String query) {
        if (query == null) return "";

        // Remove special characters that might interfere with search
        return query.trim()
                .replaceAll("[\"'`]", "") // Remove quotes
                .replaceAll("\\s+", " "); // Normalize whitespace
    }

    private UserSearchResponse convertToUserSearchResponse(User user) {
        UserSearchResponse response = modelMapper.map(user, UserSearchResponse.class);

        // Add follower count
        response.setFollowersCount(user.getFollowerCount());

        // Check if current user follows this user
        try {
            User currentUser = userService.getCurrentUser();
            response.setFollowedByCurrentUser(
                    followService.isFollowing(currentUser.getId(), user.getId())
            );
        } catch (Exception e) {
            response.setFollowedByCurrentUser(false);
        }

        return response;
    }

    private UserProfileResponse convertToUserProfileResponse(UserSearchResponse userSearchResponse) {
        return modelMapper.map(userSearchResponse, UserProfileResponse.class);
    }
}
