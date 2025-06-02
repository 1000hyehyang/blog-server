package com.thousandhyehyang.blog.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 생성 요청")
public class CommentCreateRequest {

    @Schema(description = "닉네임", example = "방문자", maxLength = 20)
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
    private String nickname;

    @Schema(description = "댓글 내용", example = "좋은 글 감사합니다!", maxLength = 200)
    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 200, message = "댓글 내용은 200자 이하여야 합니다.")
    private String content;

    @Schema(description = "이모지", example = "🐱", maxLength = 10)
    @NotBlank(message = "이모지는 필수입니다.")
    @Size(max = 10, message = "이모지는 10자 이하여야 합니다.")
    private String emoji;

    @Schema(description = "배경색", example = "#FFE0E0", maxLength = 10)
    @NotBlank(message = "배경색은 필수입니다.")
    @Size(max = 10, message = "배경색은 10자 이하여야 합니다.")
    private String bgColor;

    // 기본 생성자
    public CommentCreateRequest() {
    }

    // 테스트용 생성자
    public CommentCreateRequest(String nickname, String content, String emoji, String bgColor) {
        this.nickname = nickname;
        this.content = content;
        this.emoji = emoji;
        this.bgColor = bgColor;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }
}
