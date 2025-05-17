package com.thousandhyehyang.blog.dto;

import com.thousandhyehyang.blog.entity.FileMetadata;
import com.thousandhyehyang.blog.entity.Post;
import com.thousandhyehyang.blog.entity.PostFileMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record PostDetailResponse(
        Long id,
        String title,
        String category,
        String content,
        String html,
        String thumbnailUrl,
        String author,
        List<String> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Map<String, List<FileInfo>> attachments
) {
    /**
     * Post 엔티티와 파일 매핑으로부터 PostDetailResponse 생성
     * 
     * @param post 게시글 엔티티
     * @param fileMappings 게시글의 파일 매핑
     * @return PostDetailResponse
     */
    public static PostDetailResponse from(Post post, List<PostFileMapping> fileMappings) {
        // 참조 유형별로 파일 매핑 그룹화
        Map<String, List<FileInfo>> attachments = fileMappings.stream()
                .collect(Collectors.groupingBy(
                        PostFileMapping::getReferenceType,
                        Collectors.mapping(
                                mapping -> new FileInfo(
                                        mapping.getFile().getId(),
                                        mapping.getFile().getPublicUrl(),
                                        mapping.getFile().getOriginalFilename(),
                                        mapping.getFile().getContentType(),
                                        mapping.getFile().getFileSize()
                                ),
                                Collectors.toList()
                        )
                ));

        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getCategory(),
                post.getContent(),
                post.getHtml(),
                post.getThumbnailUrl(),
                post.getAuthor(),
                post.getTags(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                attachments
        );
    }

    /**
     * 파일 첨부 정보
     */
    public record FileInfo(
            Long id,
            String url,
            String filename,
            String contentType,
            Long fileSize
    ) {}
}
