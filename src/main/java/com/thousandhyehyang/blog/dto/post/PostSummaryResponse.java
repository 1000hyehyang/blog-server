package com.thousandhyehyang.blog.dto.post;

import com.thousandhyehyang.blog.entity.Post;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long id,
        String title,
        String category,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String thumbnailUrl
) {
    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getCategory(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getThumbnailUrl()
        );
    }
}
