package com.veyru.adapter.in.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.veyru.application.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class NotificationControllerTest {
  @Test
  void summaryUsesNamedUnreadCountField() throws Exception {
    NotificationService notifications = mock(NotificationService.class);
    when(notifications.getUnreadCount()).thenReturn(7L);
    var mvc =
        MockMvcBuilders.standaloneSetup(new NotificationController(notifications))
            .addPlaceholderValue("api.prefix", "/api/v1")
            .build();

    mvc.perform(get("/api/v1/users/me/notifications/summary"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.unreadCount").value(7));
  }
}
