package com.thousandhyehyang.blog.common;

/**
 * 애플리케이션에서 사용되는 에러 코드를 정의하는 열거형 클래스
 * 각 에러 코드는 코드 문자열과 기본 메시지를 가집니다.
 */
public enum ErrorCode {
    // 파일 관련 에러
    INVALID_FILE("파일이 유효하지 않습니다."),
    
    // 게시글 관련 에러
    POST_NOT_FOUND("게시글을 찾을 수 없습니다."),
    
    // 파일 매핑 관련 에러
    DUPLICATE_FILE_MAPPING("이미 매핑된 파일입니다."),
    
    // 인증 관련 에러
    AUTHENTICATION_ERROR("인증에 실패했습니다."),
    
    // 유효성 검증 관련 에러
    VALIDATION_ERROR("입력값이 유효하지 않습니다."),
    
    // 서버 내부 에러
    INTERNAL_ERROR("알 수 없는 서버 오류가 발생했습니다.");
    
    private final String defaultMessage;
    
    ErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }
    
    public String getDefaultMessage() {
        return defaultMessage;
    }
    
    public String getCode() {
        return name();
    }
}