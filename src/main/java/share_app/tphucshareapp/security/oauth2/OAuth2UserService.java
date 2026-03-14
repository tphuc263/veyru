package share_app.tphucshareapp.security.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import share_app.tphucshareapp.enums.UserRole;
import share_app.tphucshareapp.model.User;
import share_app.tphucshareapp.repository.UserRepository;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
            throw new OAuth2AuthenticationException("Can't take email from provider: " + provider);
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = createUser(attributes, email, provider);
            log.info("Created new OAuth2 user from {}: {}", provider, email);
        }

        return new CustomOAuth2User(
                user,
                attributes,
                Set.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
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

        User user = new User();
        user.setEmail(email);
        user.setUsername(email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 8));
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setImageUrl(picture);
        user.setBio("Joined via " + provider);
        user.setRole(UserRole.ROLE_USER);
        user.setCreatedAt(Instant.now());
        user.setPhotoCount(0);
        user.setFollowerCount(0);
        user.setFollowingCount(0);

        return userRepository.save(user);
    }
}
