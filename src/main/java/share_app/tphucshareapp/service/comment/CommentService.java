package share_app.tphucshareapp.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import share_app.tphucshareapp.dto.request.comment.CreateCommentRequest;
import share_app.tphucshareapp.dto.request.comment.UpdateCommentRequest;
import share_app.tphucshareapp.dto.response.comment.CommentResponse;
import share_app.tphucshareapp.model.Comment;
import share_app.tphucshareapp.model.CommentLike;
import share_app.tphucshareapp.model.Photo;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.CommentLikeRepository;
import share_app.tphucshareapp.repository.CommentRepository;
import share_app.tphucshareapp.repository.PhotoRepository;
import share_app.tphucshareapp.repository.UserRepository;
import share_app.tphucshareapp.service.notification.INotificationService;
import share_app.tphucshareapp.service.user.UserAvatarCacheService;
import share_app.tphucshareapp.service.user.UserService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService implements ICommentService {
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;
    private final ModelMapper modelMapper;
    private final INotificationService notificationService;
    private final UserAvatarCacheService userAvatarCacheService;
    
    // Pattern to match @username mentions
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    @Override
    public CommentResponse createComment(String photoId, CreateCommentRequest request) {
        // Validate photo exists
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

        User currentUser = userService.getCurrentUser();

        Comment.EmbeddedUser embeddedUser = new Comment.EmbeddedUser();
        embeddedUser.setUserId(currentUser.getId());
        embeddedUser.setUsername(currentUser.getUsername());

        // Create comment
        Comment comment = new Comment();
        comment.setPhotoId(photoId);
        comment.setUserId(currentUser.getId());
        comment.setText(request.getText());
        comment.setCreatedAt(Instant.now());
        comment.setUser(embeddedUser);
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        
        // Handle nested comments (replies)
        String parentCommentId = request.getParentCommentId();
        Comment parentComment = null;
        if (parentCommentId != null && !parentCommentId.isEmpty()) {
            parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found with ID: " + parentCommentId));
            comment.setParentCommentId(parentCommentId);
        }
        
        // Extract mentioned users from text
        List<String> mentionedUserIds = extractMentionedUserIds(request.getText());
        comment.setMentionedUserIds(mentionedUserIds);

        Comment savedComment = commentRepository.save(comment);
        
        // Update parent comment reply count if this is a reply
        if (parentComment != null) {
            Query parentQuery = new Query(Criteria.where("_id").is(parentCommentId));
            Update parentUpdate = new Update().inc("replyCount", 1);
            mongoTemplate.updateFirst(parentQuery, parentUpdate, Comment.class);
            
            // Send notification for reply
            notificationService.sendReplyCommentNotification(
                    parentComment.getUserId(),
                    currentUser,
                    photoId,
                    savedComment.getId(),
                    photo.getImageUrl()
            );
        } else {
            // Only increment photo comment count for top-level comments
            Query query = new Query(Criteria.where("_id").is(photoId));
            Update update = new Update().inc("commentCount", 1);
            mongoTemplate.updateFirst(query, update, Photo.class);
        }
        
        // Send notification to photo owner for new comment (only for top-level comments)
        if (parentComment == null && photo.getUser() != null) {
            notificationService.sendCommentPhotoNotification(
                    photo.getUser().getUserId(),
                    currentUser,
                    photoId,
                    savedComment.getId(),
                    photo.getImageUrl()
            );
        }
        
        // Send notifications to mentioned users
        for (String mentionedUserId : mentionedUserIds) {
            notificationService.sendMentionNotification(
                    mentionedUserId,
                    currentUser,
                    photoId,
                    savedComment.getId(),
                    photo.getImageUrl()
            );
        }
        
        log.info("Comment created successfully by user {} on photo {}", currentUser.getId(), photoId);

        return convertToCommentResponse(savedComment, currentUser.getId());
    }

    @Override
    public CommentResponse updateComment(String commentId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));

        User currentUser = userService.getCurrentUser();

        // Check if current user is the owner of the comment
        if (!comment.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update your own comments");
        }

        comment.setText(request.getText());
        
        // Update mentioned users
        List<String> mentionedUserIds = extractMentionedUserIds(request.getText());
        comment.setMentionedUserIds(mentionedUserIds);
        
        Comment updatedComment = commentRepository.save(comment);
        log.info("Comment {} updated successfully by user {}", commentId, currentUser.getId());

        return convertToCommentResponse(updatedComment, currentUser.getId());
    }

    @Override
    public void deleteComment(String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));

        User currentUser = userService.getCurrentUser();

        // Check if current user is the owner of the comment
        if (!comment.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own comments");
        }

        // Delete all replies if this is a parent comment
        if (comment.getParentCommentId() == null) {
            commentRepository.deleteAllByParentCommentId(commentId);
        }
        
        // Delete comment likes
        commentLikeRepository.deleteAllByCommentId(commentId);

        commentRepository.delete(comment);
        
        // Update counts
        if (comment.getParentCommentId() != null) {
            // This is a reply, decrement parent's reply count
            Query parentQuery = new Query(Criteria.where("_id").is(comment.getParentCommentId()));
            Update parentUpdate = new Update().inc("replyCount", -1);
            mongoTemplate.updateFirst(parentQuery, parentUpdate, Comment.class);
        } else {
            // This is a top-level comment, decrement photo's comment count
            Query query = new Query(Criteria.where("_id").is(comment.getPhotoId()));
            Update update = new Update().inc("commentCount", -1);
            mongoTemplate.updateFirst(query, update, Photo.class);
        }
        
        log.info("Comment {} deleted successfully by user {}", commentId, currentUser.getId());
    }

    @Override
    public List<CommentResponse> getPhotoComments(String photoId) {
        // Validate photo exists
        photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with ID: " + photoId));

        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
            // User not authenticated, continue without user context
        }
        
        String currentUserId = currentUser != null ? currentUser.getId() : null;

        // Get only top-level comments (no parent)
        List<Comment> topLevelComments = commentRepository.findByPhotoIdAndParentCommentIdIsNullOrderByCreatedAtAsc(photoId);
        
        return topLevelComments.stream()
                .map(comment -> {
                    CommentResponse response = convertToCommentResponse(comment, currentUserId);
                    // Load all nested replies recursively
                    loadNestedReplies(response, currentUserId);
                    return response;
                })
                .toList();
    }

    // Recursive method to load all nested replies
    private void loadNestedReplies(CommentResponse parentResponse, String currentUserId) {
        List<Comment> replies = commentRepository.findByParentCommentIdOrderByCreatedAtAsc(parentResponse.getId());

        List<CommentResponse> replyResponses = replies.stream()
                .map(reply -> {
                    CommentResponse replyResponse = convertToCommentResponse(reply, currentUserId);
                    // Recursively load nested replies
                    loadNestedReplies(replyResponse, currentUserId);
                    return replyResponse;
                })
                .toList();

        parentResponse.setReplies(replyResponses);
    }
    
    @Override
    public List<CommentResponse> getCommentReplies(String commentId, int page, int size) {
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));
        
        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
            // User not authenticated
        }
        String currentUserId = currentUser != null ? currentUser.getId() : null;
        
        List<Comment> replies = commentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId);
        return replies.stream()
                .map(reply -> convertToCommentResponse(reply, currentUserId))
                .toList();
    }

    @Override
    public long getPhotoCommentsCount(String photoId) {
        return commentRepository.countByPhotoId(photoId);
    }

    @Override
    public CommentResponse getComment(String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));

        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
            // User not authenticated
        }
        String currentUserId = currentUser != null ? currentUser.getId() : null;

        return convertToCommentResponse(comment, currentUserId);
    }
    
    // Like/Unlike comment
    @Override
    public CommentResponse toggleCommentLike(String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));
        
        User currentUser = userService.getCurrentUser();
        
        boolean alreadyLiked = commentLikeRepository.existsByCommentIdAndUserId(commentId, currentUser.getId());
        
        if (alreadyLiked) {
            // Unlike
            CommentLike like = commentLikeRepository.findByCommentIdAndUserId(commentId, currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Like not found"));
            commentLikeRepository.delete(like);
            
            Query query = new Query(Criteria.where("_id").is(commentId));
            Update update = new Update().inc("likeCount", -1);
            mongoTemplate.updateFirst(query, update, Comment.class);
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            
            log.info("User {} unliked comment {}", currentUser.getId(), commentId);
        } else {
            // Like
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(currentUser.getId());
            like.setCreatedAt(Instant.now());
            commentLikeRepository.save(like);
            
            Query query = new Query(Criteria.where("_id").is(commentId));
            Update update = new Update().inc("likeCount", 1);
            mongoTemplate.updateFirst(query, update, Comment.class);
            comment.setLikeCount(comment.getLikeCount() + 1);
            
            // Send notification
            notificationService.sendLikeCommentNotification(
                    comment.getUserId(),
                    currentUser,
                    comment.getPhotoId(),
                    commentId
            );
            
            log.info("User {} liked comment {}", currentUser.getId(), commentId);
        }
        
        return convertToCommentResponse(comment, currentUser.getId());
    }

    // Helper methods
    private List<String> extractMentionedUserIds(String text) {
        List<String> mentionedUserIds = new ArrayList<>();
        Matcher matcher = MENTION_PATTERN.matcher(text);
        Set<String> usernames = matcher.results()
                .map(result -> result.group(1))
                .collect(Collectors.toSet());
        
        for (String username : usernames) {
            userRepository.findByUsername(username).ifPresent(user -> 
                    mentionedUserIds.add(user.getId())
            );
        }
        
        return mentionedUserIds;
    }
    
    private CommentResponse convertToCommentResponse(Comment comment, String currentUserId) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setPhotoId(comment.getPhotoId());
        response.setUserId(comment.getUserId());
        response.setText(comment.getText());
        response.setCreatedAt(comment.getCreatedAt());
        response.setParentCommentId(comment.getParentCommentId());
        response.setLikeCount(comment.getLikeCount());
        response.setReplyCount(comment.getReplyCount());
        
        if (comment.getUser() != null) {
            response.setUsername(comment.getUser().getUsername());
            response.setUserImageUrl(userAvatarCacheService.getAvatar(comment.getUser().getUserId()));
        }
        
        // Check if current user liked this comment
        if (currentUserId != null) {
            response.setLikedByCurrentUser(
                    commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), currentUserId)
            );
        }
        
        // Convert mentioned user IDs to response format
        if (comment.getMentionedUserIds() != null && !comment.getMentionedUserIds().isEmpty()) {
            List<CommentResponse.MentionedUser> mentionedUsers = new ArrayList<>();
            for (String userId : comment.getMentionedUserIds()) {
                userRepository.findById(userId).ifPresent(user -> {
                    CommentResponse.MentionedUser mu = new CommentResponse.MentionedUser();
                    mu.setUserId(user.getId());
                    mu.setUsername(user.getUsername());
                    mentionedUsers.add(mu);
                });
            }
            response.setMentionedUsers(mentionedUsers);
        }
        
        return response;
    }

    private List<CommentResponse> convertToCommentResponses(List<Comment> comments) {
        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
            // User not authenticated
        }
        String currentUserId = currentUser != null ? currentUser.getId() : null;
        
        return comments.stream()
                .map(comment -> convertToCommentResponse(comment, currentUserId))
                .toList();
    }
}
