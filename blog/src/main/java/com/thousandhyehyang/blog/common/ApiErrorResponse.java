package com.thousandhyehyang.blog.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API 오류 응답 공통 형식")
public class ApiErrorResponse {
    @Schema(description = "성공 여부", example = "false")
    private final boolean success = false;

    @Schema(description = "오류 코드", example = "VALIDATION_ERROR")
    private final String code;

    @Schema(description = "오류 메시지", example = "파일이 비어 있습니다.")
    private final String message;

    public ApiErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
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
}
