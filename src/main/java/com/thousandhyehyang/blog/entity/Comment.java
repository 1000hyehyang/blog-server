package com.thousandhyehyang.blog.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false, length = 200)
    private String content;

    @Column(nullable = false, length = 10)
    private String emoji;

    @Column(nullable = false, length = 10)
    private String bgColor;

    protected Comment() {
    }

    public Comment(Post post, String nickname, String content, String emoji, String bgColor) {
        this.post = post;
        this.nickname = nickname;
        this.content = content;
        this.emoji = emoji;
        this.bgColor = bgColor;
    }

    /**
     * 댓글 내용 업데이트
     * 
     * @param nickname 새 닉네임 (null인 경우 업데이트하지 않음)
     * @param content 새 내용 (null인 경우 업데이트하지 않음)
     * @param emoji 새 이모지 (null인 경우 업데이트하지 않음)
     * @param bgColor 새 배경색 (null인 경우 업데이트하지 않음)
     */
    public void update(String nickname, String content, String emoji, String bgColor) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (content != null) {
            this.content = content;
        }
        if (emoji != null) {
            this.emoji = emoji;
        }
        if (bgColor != null) {
            this.bgColor = bgColor;
        }
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
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
}
