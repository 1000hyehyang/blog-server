package com.thousandhyehyang.blog.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@SQLDelete(sql = "UPDATE posts SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private String category;

    @Lob
    @Column(nullable = false)
    private String content;

    @Lob
    @Column(nullable = false)
    private String html;

    @Column
    private String thumbnailUrl;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private boolean deleted = false;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags = new ArrayList<>();

    protected Post() {
    }

    public Post(String title, String category, String content, String html, String thumbnailUrl, String author) {
        this.title = title;
        this.category = category;
        this.content = content;
        this.html = html;
        this.thumbnailUrl = thumbnailUrl;
        this.author = author;
    }

    /**
     * 태그를 추가합니다.
     *
     * @param tag 추가할 태그
     */
    public void addTag(String tag) {
        if (tag != null && !tag.isEmpty()) {
            this.postTags.add(new PostTag(this, tag));
        }
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getContent() {
        return content;
    }

    public String getHtml() {
        return html;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getAuthor() {
        return author;
    }

    public List<String> getTags() {
        return postTags.stream()
                .map(PostTag::getTag)
                .toList();
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * 게시글 내용 업데이트
     * 
     * @param title 새 제목 (null인 경우 업데이트하지 않음)
     * @param category 새 카테고리 (null인 경우 업데이트하지 않음)
     * @param content 새 내용 (null인 경우 업데이트하지 않음)
     * @param html 새 HTML 내용 (null인 경우 업데이트하지 않음)
     * @param thumbnailUrl 새 썸네일 URL (null인 경우 업데이트하지 않음)
     */
    public void update(String title, String category, String content, String html, String thumbnailUrl) {
        if (title != null) {
            this.title = title;
        }
        if (category != null) {
            this.category = category;
        }
        if (content != null) {
            this.content = content;
        }
        if (html != null) {
            this.html = html;
        }
        this.thumbnailUrl = thumbnailUrl; // thumbnailUrl can be null
    }

    /**
     * 게시글의 모든 태그를 제거합니다.
     */
    public void clearTags() {
        this.postTags.clear();
    }
}
