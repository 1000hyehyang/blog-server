package com.thousandhyehyang.blog.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API 응답 공통 형식")
public class ApiResponse<T> {
    @Schema(description = "성공 여부", example = "true")
    private final boolean success = true;

    @Schema(description = "응답 데이터")
    private final T data;

    public ApiResponse(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }
}
