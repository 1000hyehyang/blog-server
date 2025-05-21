package com.thousandhyehyang.blog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "게시글 수정 요청")
public record PostUpdateRequest(
        @Schema(description = "제목", example = "수정된 게시글 제목", maxLength = 100)
        @Size(min = 1, max = 100, message = "제목은 1자 이상 100자 이하로 입력해주세요.")
        String title,

        @Schema(description = "카테고리", example = "기술", maxLength = 50)
        @Size(min = 1, max = 50, message = "카테고리는 1자 이상 50자 이하로 입력해주세요.")
        String category,

        @Schema(description = "내용", example = "수정된 게시글 내용")
        String content,

        @Schema(description = "HTML 내용", example = "<p>수정된 게시글 내용</p>")
        String html,

        @Schema(description = "썸네일 URL", example = "https://example.com/image.jpg")
        @Pattern(regexp = "^(https?://.*|)$", message = "썸네일 URL은 유효한 URL 형식이어야 합니다.")
        String thumbnailUrl,

        @Schema(description = "태그 목록", example = "[\"태그1\", \"태그2\"]")
        @Valid
        @NotNull(message = "태그 목록은 null이 될 수 없습니다. 빈 목록을 사용하세요.")
        List<@Size(max = 20, message = "태그는 20자 이하로 입력해주세요.") String> tags,

        @Schema(description = "임시저장 여부", example = "false")
        Boolean draft
) {
}
