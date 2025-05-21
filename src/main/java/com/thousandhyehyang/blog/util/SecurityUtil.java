package com.thousandhyehyang.blog.util;

import com.thousandhyehyang.blog.entity.Account;
import com.thousandhyehyang.blog.exception.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 인증된 사용자 정보를 제공하는 유틸리티 클래스
 */
@Component
public class SecurityUtil {

    /**
     * 현재 인증된 사용자 정보 가져오기
     * Spring Security의 SecurityContext에서 현재 인증된 사용자 정보를 추출합니다.
     * 
     * @return 인증된 사용자의 닉네임
     * @throws AuthenticationException 인증된 사용자 정보가 없거나 유효하지 않은 경우
     */
    public String getCurrentUserNickname() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AuthenticationException("인증 정보가 존재하지 않습니다.");
        }

        if (!(authentication.getPrincipal() instanceof Account)) {
            throw new AuthenticationException("유효하지 않은 인증 정보입니다.");
        }

        Account account = (Account) authentication.getPrincipal();
        return account.getNickname();
    }
}