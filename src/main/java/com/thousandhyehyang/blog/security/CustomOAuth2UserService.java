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

        // 제공자 세부 정보 추출
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 사용자 세부 정보 추출
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 제공자 기반 사용자 정보 처리
        OAuth2UserInfo userInfo;
        if (provider.equals("google")) {
            userInfo = new GoogleOAuth2UserInfo(attributes);
        } else if (provider.equals("github")) {
            userInfo = new GithubOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationException("Login with " + provider + " is not supported");
        }

        // 이메일이 null일 수 있으므로 식별을 위해 사용자 이름 사용
        if (!StringUtils.hasText(userInfo.getId())) {
            throw new OAuth2AuthenticationException("User ID not found from OAuth2 provider");
        }

        // 사용자 저장 또는 업데이트
        Account account = saveOrUpdateUser(userInfo, provider);

        // Account 엔티티에 OAuth2 속성 설정
        account.setAttributes(attributes);

        // Account 엔티티를 OAuth2User로 반환
        return account;
    }

    private Account saveOrUpdateUser(OAuth2UserInfo userInfo, String provider) {
        String email = userInfo.getEmail();

        // 제공자에 관계없이 이메일로 찾기 시도
        Optional<Account> accountOptional = Optional.empty();
        if (StringUtils.hasText(email)) {
            accountOptional = accountRepository.findByEmail(email);
        }

        Account account;
        if (accountOptional.isPresent()) {
            // 기존 계정 업데이트
            account = accountOptional.get();
            account.updateProfile(userInfo.getName(), userInfo.getImageUrl());
        } else {
            // 닉네임이 이름으로 초기화된 새 계정 생성
            account = new Account(
                    provider,
                    email, // 이메일은 null일 수 있음
                    userInfo.getName(),
                    userInfo.getName(), // 이름으로 닉네임 초기화
                    userInfo.getImageUrl(),
                    Account.Role.USER // 기본 역할
            );
        }

        return accountRepository.save(account);
    }

    // OAuth2 사용자 정보를 위한 인터페이스
    interface OAuth2UserInfo {
        String getId();

        String getName();

        String getEmail();

        String getImageUrl();
    }

    // Google 구현
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

    // GitHub 구현
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
