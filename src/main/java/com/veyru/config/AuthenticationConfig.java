package com.veyru.config;

import com.veyru.adapter.in.security.userdetails.AppUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthenticationConfig {
  @Bean
  DaoAuthenticationProvider authenticationProvider(
      AppUserDetailsService users, PasswordEncoder passwords) {
    var provider = new DaoAuthenticationProvider(users);
    provider.setPasswordEncoder(passwords);
    return provider;
  }
}
