package com.thousandhyehyang.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostCreateRequest(
        @NotBlank @Size(max = 100) String title,
        @NotBlank String category,
        @NotBlank String content,
        @NotBlank String html,
        String thumbnailUrl,
        List<@Size(max = 20) String> tags
) {}
