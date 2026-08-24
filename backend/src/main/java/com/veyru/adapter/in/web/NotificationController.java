package com.veyru.adapter.in.web;

import com.veyru.adapter.in.dto.response.notification.NotificationResponse;
import com.veyru.adapter.in.dto.response.notification.NotificationSummaryResponse;
import com.veyru.application.notification.NotificationService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/users/me/notifications")
public class NotificationController {
  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<List<NotificationResponse>> getNotifications(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    List<NotificationResponse> notifications =
        notificationService.getNotifications(page, size).stream()
            .map(NotificationResponse::from)
            .toList();
    return ResponseEntity.ok(notifications);
  }

  @GetMapping("/summary")
  public ResponseEntity<NotificationSummaryResponse> getSummary() {
    return ResponseEntity.ok(new NotificationSummaryResponse(notificationService.getUnreadCount()));
  }

  @PatchMapping("/{notificationId}")
  public ResponseEntity<Void> markAsRead(@PathVariable String notificationId) {
    notificationService.markAsRead(notificationId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping
  public ResponseEntity<Void> markAllAsRead() {
    notificationService.markAllAsRead();
    return ResponseEntity.noContent().build();
  }

  public NotificationController(final NotificationService notificationService) {
    this.notificationService = notificationService;
  }
}
