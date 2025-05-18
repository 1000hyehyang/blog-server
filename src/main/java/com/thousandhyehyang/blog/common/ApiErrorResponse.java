package com.thousandhyehyang.blog.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API 오류 응답 공통 형식")
public class ApiErrorResponse {
    @Schema(description = "성공 여부", example = "false")
    private final boolean success = false;

    @Schema(description = "오류 코드", example = "VALIDATION_ERROR")
    private final String code;

    @Schema(description = "오류 메시지", example = "입력값이 유효하지 않습니다.")
    private final String message;

    @Schema(description = "디버그 메시지 (선택적)", example = "title: 제목은 필수 입력 항목입니다.")
    private final String debugMessage;

    /**
     * ErrorCode와 사용자 정의 메시지로 응답 객체 생성
     * @param errorCode 에러 코드 열거형
     * @param message 사용자 정의 메시지
     */
    public ApiErrorResponse(ErrorCode errorCode, String message) {
        this.code = errorCode.getCode();
        this.message = message;
        this.debugMessage = null;
    }

    /**
     * ErrorCode와 사용자 정의 메시지, 디버그 메시지로 응답 객체 생성
     * @param errorCode 에러 코드 열거형
     * @param message 사용자 정의 메시지
     * @param debugMessage 디버그용 상세 메시지
     */
    public ApiErrorResponse(ErrorCode errorCode, String message, String debugMessage) {
        this.code = errorCode.getCode();
        this.message = message;
        this.debugMessage = debugMessage;
    }

    /**
     * ErrorCode의 기본 메시지로 응답 객체 생성
     * @param errorCode 에러 코드 열거형
     */
    public ApiErrorResponse(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getDefaultMessage();
        this.debugMessage = null;
    }

    /**
     * ErrorCode의 기본 메시지와 디버그 메시지로 응답 객체 생성
     * @param errorCode 에러 코드 열거형
     * @param debugMessage 디버그용 상세 메시지
     */
    public ApiErrorResponse(ErrorCode errorCode, String debugMessage, boolean isDebugMessage) {
        this.code = errorCode.getCode();
        this.message = errorCode.getDefaultMessage();
        this.debugMessage = debugMessage;
    }

    /**
     * 하위 호환성을 위한 생성자 (String 코드 사용)
     * @param code 에러 코드 문자열
     * @param message 에러 메시지
     * @deprecated ErrorCode enum을 사용하는 생성자를 사용하세요
     */
    @Deprecated
    public ApiErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
        this.debugMessage = null;
    }

    /**
     * 정적 팩토리 메서드 - ErrorCode로부터 응답 생성
     */
    public static ApiErrorResponse of(ErrorCode errorCode) {
        return new ApiErrorResponse(errorCode);
    }

    /**
     * 정적 팩토리 메서드 - ErrorCode와 사용자 정의 메시지로 응답 생성
     */
    public static ApiErrorResponse of(ErrorCode errorCode, String message) {
        return new ApiErrorResponse(errorCode, message);
    }

    /**
     * 정적 팩토리 메서드 - ErrorCode와 디버그 메시지로 응답 생성
     */
    public static ApiErrorResponse withDebugMessage(ErrorCode errorCode, String debugMessage) {
        return new ApiErrorResponse(errorCode, debugMessage, true);
    }

    /**
     * 정적 팩토리 메서드 - ErrorCode, 사용자 정의 메시지, 디버그 메시지로 응답 생성
     */
    public static ApiErrorResponse of(ErrorCode errorCode, String message, String debugMessage) {
        return new ApiErrorResponse(errorCode, message, debugMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDebugMessage() {
        return debugMessage;
    }
}
