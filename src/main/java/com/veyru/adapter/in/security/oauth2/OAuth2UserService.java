package com.veyru.adapter.in.security.oauth2;

import com.veyru.application.port.out.UserStore;
import com.veyru.domain.model.User;
import java.time.Clock;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class OAuth2UserService extends DefaultOAuth2UserService {
  private static final Logger log = LoggerFactory.getLogger(OAuth2UserService.class);
  private final UserStore userStore;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);
    String provider = userRequest.getClientRegistration().getRegistrationId();
    return processOAuth2User(provider, oAuth2User);
  }

  private OAuth2User processOAuth2User(String provider, OAuth2User oAuth2User) {
    Map<String, Object> attributes = oAuth2User.getAttributes();
    String email = extractEmail(attributes, provider);
    if (!StringUtils.hasText(email)) {
      throw new OAuth2AuthenticationException("Can\'t take email from provider: " + provider);
    }
    User user = userStore.findByEmail(email).orElse(null);
    if (user == null) {
      user = createUser(attributes, email, provider);
      log.info("Created new OAuth2 user from {}: {}", provider, email);
    }
    return new CustomOAuth2User(
        user, attributes, Set.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
  }

  private String extractEmail(Map<String, Object> attributes, String provider) {
    if ("google".equalsIgnoreCase(provider)) {
      return (String) attributes.get("email");
    } else if ("facebook".equalsIgnoreCase(provider)) {
      return (String) attributes.get("email");
    }
    return null;
  }

  private User createUser(Map<String, Object> attributes, String email, String provider) {
    String picture = null;
    if ("google".equalsIgnoreCase(provider)) {
      picture = (String) attributes.get("picture");
    } else if ("facebook".equalsIgnoreCase(provider)) {
      Object pictureObj = attributes.get("picture");
      if (pictureObj instanceof Map) {
        Object dataObj = ((Map<?, ?>) pictureObj).get("data");
        if (dataObj instanceof Map) {
          Object urlObj = ((Map<?, ?>) dataObj).get("url");
          if (urlObj instanceof String) {
            picture = (String) urlObj;
          }
        }
      }
    }
    User user =
        User.oauthRegistered(
            email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 8),
            email,
            passwordEncoder.encode(UUID.randomUUID().toString()),
            picture,
            "Joined via " + provider,
            clock.instant());
    return userStore.save(user);
  }

  public OAuth2UserService(
      final UserStore userStore, final PasswordEncoder passwordEncoder, final Clock clock) {
    this.userStore = userStore;
    this.passwordEncoder = passwordEncoder;
    this.clock = clock;
  }
}
