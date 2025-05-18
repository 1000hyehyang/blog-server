package com.thousandhyehyang.blog.exception;

/**
 * 인증 관련 오류가 발생했을 때 발생하는 예외
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException() {
        super("인증된 사용자 정보를 찾을 수 없습니다.");
    }
}