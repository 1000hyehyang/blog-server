package com.thousandhyehyang.blog.security;

import com.thousandhyehyang.blog.entity.Account;
import com.thousandhyehyang.blog.repository.AccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final AccountRepository accountRepository;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    public CustomOAuth2UserService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // Extract provider details
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // Extract user details
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Process user information based on provider
        OAuth2UserInfo userInfo;
        if (provider.equals("google")) {
            userInfo = new GoogleOAuth2UserInfo(attributes);
        } else if (provider.equals("github")) {
            userInfo = new GithubOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationException("Login with " + provider + " is not supported");
        }

        // Email can be null, we'll use username for identification
        if (!StringUtils.hasText(userInfo.getId())) {
            throw new OAuth2AuthenticationException("User ID not found from OAuth2 provider");
        }

        // Save or update user
        Account account = saveOrUpdateUser(userInfo, provider);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())),
                attributes,
                userNameAttributeName
        );
    }

    private Account saveOrUpdateUser(OAuth2UserInfo userInfo, String provider) {
        String email = userInfo.getEmail();

        // Try to find by email regardless of provider
        Optional<Account> accountOptional = Optional.empty();
        if (StringUtils.hasText(email)) {
            accountOptional = accountRepository.findByEmail(email);
        }

        Account account;
        if (accountOptional.isPresent()) {
            // Update existing account
            account = accountOptional.get();
            account.updateProfile(userInfo.getName(), userInfo.getImageUrl());
        } else {
            // Create new account with nickname initialized to name
            account = new Account(
                    provider,
                    email, // Email can be null
                    userInfo.getName(),
                    userInfo.getName(), // Initialize nickname with name
                    userInfo.getImageUrl(),
                    Account.Role.USER // Default role
            );
        }

        return accountRepository.save(account);
    }

    // Interface for OAuth2 user info
    interface OAuth2UserInfo {
        String getId();
        String getName();
        String getEmail();
        String getImageUrl();
    }

    // Google implementation
    static class GoogleOAuth2UserInfo implements OAuth2UserInfo {
        private final Map<String, Object> attributes;

        GoogleOAuth2UserInfo(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        @Override
        public String getId() {
            return (String) attributes.get("sub");
        }

        @Override
        public String getName() {
            return (String) attributes.get("name");
        }

        @Override
        public String getEmail() {
            return (String) attributes.get("email");
        }

        @Override
        public String getImageUrl() {
            return (String) attributes.get("picture");
        }
    }

    // GitHub implementation
    static class GithubOAuth2UserInfo implements OAuth2UserInfo {
        private final Map<String, Object> attributes;

        GithubOAuth2UserInfo(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        @Override
        public String getId() {
            return ((Integer) attributes.get("id")).toString();
        }

        @Override
        public String getName() {
            return (String) attributes.get("name");
        }

        @Override
        public String getEmail() {
            return (String) attributes.get("email");
        }

        @Override
        public String getImageUrl() {
            return (String) attributes.get("avatar_url");
        }
    }
}
