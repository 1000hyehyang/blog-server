package com.thousandhyehyang.blog.dto.comment;

import com.thousandhyehyang.blog.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "댓글 응답")
public class CommentResponse {

    @Schema(description = "댓글 ID", example = "1")
    private final Long id;

    @Schema(description = "닉네임", example = "방문자")
    private final String nickname;

    @Schema(description = "댓글 내용", example = "좋은 글 감사합니다!")
    private final String content;

    @Schema(description = "이모지", example = "🐱")
    private final String emoji;

    @Schema(description = "배경색", example = "#FFE0E0")
    private final String bgColor;

    @Schema(description = "생성 시간", example = "2023-01-01T12:00:00")
    private final LocalDateTime createdAt;

    public CommentResponse(Long id, String nickname, String content, String emoji, String bgColor, LocalDateTime createdAt) {
        this.id = id;
        this.nickname = nickname;
        this.content = content;
        this.emoji = emoji;
        this.bgColor = bgColor;
        this.createdAt = createdAt;
    }

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getNickname(),
                comment.getContent(),
                comment.getEmoji(),
                comment.getBgColor(),
                comment.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getContent() {
        return content;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getBgColor() {
        return bgColor;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}