package com.veyru.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.veyru.adapter.in.security.jwt.AuthTokenFilter;
import com.veyru.adapter.in.security.jwt.JwtAccessDeniedHandler;
import com.veyru.adapter.in.security.jwt.JwtEntryPoint;
import com.veyru.adapter.in.security.oauth2.OAuth2FailureHandler;
import com.veyru.adapter.in.security.oauth2.OAuth2SuccessHandler;
import com.veyru.adapter.in.security.userdetails.AppUserDetailsService;
import com.veyru.adapter.in.web.CommentController;
import com.veyru.adapter.in.web.FollowController;
import com.veyru.adapter.in.web.ShareController;
import com.veyru.adapter.in.web.UserController;
import com.veyru.application.identity.UserProfileService;
import com.veyru.application.result.comment.CommentResult;
import com.veyru.application.result.user.UserProfileResult;
import com.veyru.application.social.CommentService;
import com.veyru.application.social.FollowService;
import com.veyru.application.social.ShareService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = {
      CommentController.class,
      FollowController.class,
      ShareController.class,
      UserController.class
    })
@Import({SecurityConfig.class, JwtEntryPoint.class, JwtAccessDeniedHandler.class})
@TestPropertySource(properties = "api.prefix=/api/v1")
class PublicReadSecurityTest {
  @Autowired private MockMvc mvc;

  @MockitoBean private CommentService comments;
  @MockitoBean private FollowService follows;
  @MockitoBean private ShareService shares;
  @MockitoBean private UserProfileService users;
  @MockitoBean private AppUserDetailsService userDetailsService;
  @MockitoBean private AuthTokenFilter authTokenFilter;
  @MockitoBean private PasswordEncoder passwordEncoder;
  @MockitoBean private DaoAuthenticationProvider authenticationProvider;
  @MockitoBean private OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;
  @MockitoBean private OAuth2SuccessHandler oAuth2SuccessHandler;
  @MockitoBean private OAuth2FailureHandler oAuth2FailureHandler;
  @MockitoBean private ClientRegistrationRepository clientRegistrationRepository;

  @BeforeEach
  void passRequestsThroughTheMockedJwtFilter() throws Exception {
    doAnswer(
            invocation -> {
              invocation
                  .<jakarta.servlet.FilterChain>getArgument(2)
                  .doFilter(invocation.getArgument(0), invocation.getArgument(1));
              return null;
            })
        .when(authTokenFilter)
        .doFilter(any(), any(), any());
  }

  @Test
  void anonymousCanReadPublicRoutesButNotMeRoutes() throws Exception {
    CommentResult comment = new CommentResult();
    comment.setId("comment");
    when(comments.getComment("comment")).thenReturn(comment);
    when(comments.getPhotoCommentsCount("photo")).thenReturn(0L);
    when(follows.getFollowers("user", 0, 20)).thenReturn(List.of());
    when(shares.getPhotoShares("photo")).thenReturn(List.of());
    when(users.getUserProfileById("user"))
        .thenReturn(new UserProfileResult("user", "user", null, Map.of(), null, false));

    mvc.perform(get("/api/v1/comments/comment")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/photos/photo/comments/count")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/users/user/followers")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/photos/photo/shares")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/users/user")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/v1/photos/photo/shares/me")).andExpect(status().isUnauthorized());
  }
}
