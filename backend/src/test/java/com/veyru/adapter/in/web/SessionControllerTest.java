package com.veyru.adapter.in.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.veyru.application.identity.AuthenticatedUser;
import com.veyru.application.identity.SessionService;
import com.veyru.application.identity.SessionTokens;
import com.veyru.config.AuthProperties;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SessionControllerTest {
  @Test
  void loginReturnsUserAndHardenedCookies() throws Exception {
    SessionService sessions = mock(SessionService.class);
    var user = new AuthenticatedUser("user-1", "alice", "alice@example.com", "ROLE_USER");
    when(sessions.login(new com.veyru.application.identity.LoginCommand("alice", "password")))
        .thenReturn(new SessionTokens(user, "access", "refresh", 900));
    MockMvc mvc =
        MockMvcBuilders.standaloneSetup(
                new SessionController(
                    sessions,
                    new SessionCookieManager(
                        new AuthProperties(
                            new AuthProperties.Token(
                                "VGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS0zMi1ieXRlcw==",
                                Duration.ofMinutes(15)),
                            new AuthProperties.Cookie(false)),
                        "/api/v1")))
            .addPlaceholderValue("api.prefix", "/api/v1")
            .build();

    mvc.perform(
            post("/api/v1/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"alice\",\"password\":\"password\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("alice@example.com"))
        .andExpect(
            header()
                .stringValues(
                    HttpHeaders.SET_COOKIE,
                    org.hamcrest.Matchers.hasItems(
                        org.hamcrest.Matchers.allOf(
                            org.hamcrest.Matchers.containsString("veyru_access=access"),
                            org.hamcrest.Matchers.containsString("HttpOnly"),
                            org.hamcrest.Matchers.containsString("SameSite=Lax"),
                            org.hamcrest.Matchers.containsString("Path=/")),
                        org.hamcrest.Matchers.allOf(
                            org.hamcrest.Matchers.containsString("veyru_refresh=refresh"),
                            org.hamcrest.Matchers.containsString("HttpOnly"),
                            org.hamcrest.Matchers.containsString("Path=/api/v1/sessions")))));
  }
}
