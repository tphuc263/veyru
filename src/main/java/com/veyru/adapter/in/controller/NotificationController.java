package com.veyru.adapter.in.controller;

import com.veyru.adapter.in.dto.response.notification.NotificationResponse;
import com.veyru.domain.service.notification.NotificationService;
import com.veyru.domain.service.user.UserService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/users/me/notifications")
public class NotificationController {
  private final NotificationService notificationService;
  private final UserService userService;

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
      final NotificationService notificationService, final UserService userService) {
    this.notificationService = notificationService;
    this.userService = userService;
  }
}
