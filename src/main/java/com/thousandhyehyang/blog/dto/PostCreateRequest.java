package com.thousandhyehyang.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PostCreateRequest(
        @NotBlank(message = "제목은 필수 입력값입니다.")
        @Size(min = 1, max = 100, message = "제목은 1자 이상 100자 이하로 입력해주세요.")
        String title,

        @NotBlank(message = "카테고리는 필수 입력값입니다.")
        @Size(min = 1, max = 50, message = "카테고리는 1자 이상 50자 이하로 입력해주세요.")
        String category,

        @NotBlank(message = "내용은 필수 입력값입니다.")
        String content,

        @NotBlank(message = "HTML 내용은 필수 입력값입니다.")
        String html,

        @Pattern(regexp = "^(https?://.*|)$", message = "썸네일 URL은 유효한 URL 형식이어야 합니다.")
        String thumbnailUrl,

        @Valid
        @NotNull(message = "태그 목록은 null이 될 수 없습니다. 빈 목록을 사용하세요.")
        List<@Size(max = 20, message = "태그는 20자 이하로 입력해주세요.") String> tags,

        boolean draft
) {
}
