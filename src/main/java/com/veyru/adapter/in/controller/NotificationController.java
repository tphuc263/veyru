package com.veyru.adapter.in.controller;

import com.veyru.application.identity.UserProfileService;
import com.veyru.application.notification.NotificationService;
import com.veyru.application.result.notification.NotificationResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/users/me/notifications")
public class NotificationController {
  private final NotificationService notificationService;
  private final UserProfileService userService;

  @GetMapping
  public ResponseEntity<List<NotificationResponse>> getNotifications(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    String userId = userService.getCurrentUser().getId();
    List<NotificationResponse> notifications =
        notificationService.getNotifications(userId, page, size);
    return ResponseEntity.ok(notifications);
  }

  @PatchMapping("/{notificationId}")
  public ResponseEntity<Void> markAsRead(@PathVariable String notificationId) {
    notificationService.markAsRead(notificationId, userService.getCurrentUser().getId());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping
  public ResponseEntity<Void> markAllAsRead() {
    String userId = userService.getCurrentUser().getId();
    notificationService.markAllAsRead(userId);
    return ResponseEntity.noContent().build();
  }

  public NotificationController(
      final NotificationService notificationService, final UserProfileService userService) {
    this.notificationService = notificationService;
    this.userService = userService;
  }
}
