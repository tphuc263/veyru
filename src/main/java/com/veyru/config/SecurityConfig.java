package com.veyru.config;

import com.veyru.adapter.in.security.jwt.AuthTokenFilter;
import com.veyru.adapter.in.security.jwt.JwtAccessDeniedHandler;
import com.veyru.adapter.in.security.jwt.JwtEntryPoint;
import com.veyru.adapter.in.security.oauth2.OAuth2FailureHandler;
import com.veyru.adapter.in.security.oauth2.OAuth2SuccessHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Value("${api.prefix}")
  private String API;

  private final JwtEntryPoint authEntryPoint;
  private final JwtAccessDeniedHandler accessDeniedHandler;
  private final AuthTokenFilter authTokenFilter;
  private final DaoAuthenticationProvider authenticationProvider;
  private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;
  private final OAuth2FailureHandler oAuth2FailureHandler;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    var csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    csrfRepository.setCookieName("XSRF-TOKEN");
    csrfRepository.setHeaderName("X-XSRF-TOKEN");
    http.csrf(
            csrf ->
                csrf.csrfTokenRepository(csrfRepository)
                    .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
        .exceptionHandling(
            exception ->
                exception
                    .authenticationEntryPoint(authEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.OPTIONS)
                    .permitAll()
                    .requestMatchers(
                        API + "/csrf",
                        API + "/password-reset-requests",
                        API + "/password-resets",
                        "/login/**",
                        "/oauth2/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/actuator/health/**")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.POST,
                        API + "/sessions",
                        API + "/sessions/refresh",
                        API + "/sessions/oauth2")
                    .permitAll()
                    .requestMatchers(HttpMethod.DELETE, API + "/sessions/current")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, API + "/users")
                    .permitAll()
                    .requestMatchers(API + "/users/me/**")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, API + "/photos/*/shares/me")
                    .authenticated()
                    .requestMatchers(
                        HttpMethod.GET,
                        API + "/photos/**",
                        API + "/comments/**",
                        API + "/users/**",
                        API + "/tags/**",
                        API + "/search-suggestions/**",
                        API + "/recommendations/photos/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .authenticationProvider(authenticationProvider)
        .oauth2Login(
            oauth2 ->
                oauth2
                    .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(oAuth2FailureHandler))
        .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  public SecurityConfig(
      final JwtEntryPoint authEntryPoint,
      final JwtAccessDeniedHandler accessDeniedHandler,
      final AuthTokenFilter authTokenFilter,
      final DaoAuthenticationProvider authenticationProvider,
      final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService,
      final OAuth2SuccessHandler oAuth2SuccessHandler,
      final OAuth2FailureHandler oAuth2FailureHandler) {
    this.authEntryPoint = authEntryPoint;
    this.accessDeniedHandler = accessDeniedHandler;
    this.authTokenFilter = authTokenFilter;
    this.authenticationProvider = authenticationProvider;
    this.oAuth2UserService = oAuth2UserService;
    this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    this.oAuth2FailureHandler = oAuth2FailureHandler;
  }
}
