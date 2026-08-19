package com.veyru.controller;

import com.veyru.dto.response.notification.NotificationResponse;
import com.veyru.service.notification.INotificationService;
import com.veyru.service.user.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final INotificationService notificationService;
  private final UserService userService;

  @GetMapping
  public ResponseEntity<List<NotificationResponse>> getNotifications(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    String userId = userService.getCurrentUser().getId();
    List<NotificationResponse> notifications =
        notificationService.getNotifications(userId, page, size);
    return ResponseEntity.ok(notifications);
  }

  @GetMapping("/unread-count")
  public ResponseEntity<Long> getUnreadCount() {
    String userId = userService.getCurrentUser().getId();
    long count = notificationService.getUnreadCount(userId);
    return ResponseEntity.ok(count);
  }

  @PutMapping("/{notificationId}/read")
  public ResponseEntity<Void> markAsRead(@PathVariable String notificationId) {
    notificationService.markAsRead(notificationId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/read-all")
  public ResponseEntity<Void> markAllAsRead() {
    String userId = userService.getCurrentUser().getId();
    notificationService.markAllAsRead(userId);
    return ResponseEntity.noContent().build();
  }
}
