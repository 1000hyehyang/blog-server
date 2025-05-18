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
}
