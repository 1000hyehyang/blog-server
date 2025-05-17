package com.thousandhyehyang.blog.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API 응답 공통 형식")
public class ApiResponse<T> {
    @Schema(description = "성공 여부", example = "true")
    private final boolean success = true;

    @Schema(description = "응답 데이터")
    private final T data;

    @Schema(description = "응답 메시지")
    private final String message;

    public ApiResponse(T data) {
        this.data = data;
        this.message = null;
    }

    public ApiResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(null, message);
    }
}
