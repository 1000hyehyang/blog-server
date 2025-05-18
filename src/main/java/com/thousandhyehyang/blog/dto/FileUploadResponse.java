package com.thousandhyehyang.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "파일 업로드 응답")
public record FileUploadResponse(
        @Schema(description = "업로드된 파일의 URL", example = "https://cdn.example.com/thumbnails/123e4567-e89b-12d3-a456-426614174000-image.jpg")
        String url,

        @Schema(description = "업로드 타입 (THUMBNAIL, EDITOR_IMAGE, EDITOR_VIDEO)", example = "THUMBNAIL")
        String type,

        @Schema(description = "원본 파일명", example = "image.jpg")
        String filename
) {
}
