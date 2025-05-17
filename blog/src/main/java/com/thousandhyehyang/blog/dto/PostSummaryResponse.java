package com.thousandhyehyang.blog.dto;

import com.thousandhyehyang.blog.entity.Post;

public record PostSummaryResponse(
        Long id,
        String title,
        String category,
        String content,
        String date,
        String thumbnailUrl
) {
    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getCategory(),
                post.getContent(),
                post.getCreatedAt().toString(),
                post.getThumbnailUrl()
        );
    }
}