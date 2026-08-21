package com.veyru.config;

import com.veyru.adapter.in.security.jwt.AuthTokenFilter;
import com.veyru.adapter.in.security.jwt.JwtAccessDeniedHandler;
import com.veyru.adapter.in.security.jwt.JwtEntryPoint;
import com.veyru.adapter.in.security.oauth2.OAuth2FailureHandler;
import com.veyru.adapter.in.security.oauth2.OAuth2SuccessHandler;
import com.veyru.adapter.in.security.userdetails.AppUserDetailsService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Value("${api.prefix}")
  private String API;

  private final AppUserDetailsService userDetailsService;
  private final JwtEntryPoint authEntryPoint;
  private final JwtAccessDeniedHandler accessDeniedHandler;
  private final AuthTokenFilter authTokenFilter;
  private final PasswordEncoder passwordEncoder;
  private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;
  private final OAuth2FailureHandler oAuth2FailureHandler;

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    var authProvider = new DaoAuthenticationProvider(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder);
    return authProvider;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    List<String> securedUrls =
        List.of(
            API + "/user/**",
            API + "/photos/create",
            API + "/photos/*/delete",
            API + "/likes/**",
            API + "/comments/**",
            API + "/follows/**",
            API + "/favorites/**",
            API + "/ai/**",
            API + "/messages/**",
            API + "/newsfeed/**");
    http.csrf(AbstractHttpConfigurer::disable)
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
                    .requestMatchers(API + "/admin/**")
                    .hasAuthority("ROLE_ADMIN")
                    .requestMatchers(securedUrls.toArray(String[]::new))
                    .authenticated()
                    .requestMatchers(API + "/auth/**")
                    .permitAll()
                    .anyRequest()
                    .permitAll())
        .authenticationProvider(authenticationProvider())
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
      final AppUserDetailsService userDetailsService,
      final JwtEntryPoint authEntryPoint,
      final JwtAccessDeniedHandler accessDeniedHandler,
      final AuthTokenFilter authTokenFilter,
      final PasswordEncoder passwordEncoder,
      final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService,
      final OAuth2SuccessHandler oAuth2SuccessHandler,
      final OAuth2FailureHandler oAuth2FailureHandler) {
    this.userDetailsService = userDetailsService;
    this.authEntryPoint = authEntryPoint;
    this.accessDeniedHandler = accessDeniedHandler;
    this.authTokenFilter = authTokenFilter;
    this.passwordEncoder = passwordEncoder;
    this.oAuth2UserService = oAuth2UserService;
    this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    this.oAuth2FailureHandler = oAuth2FailureHandler;
  }
}
