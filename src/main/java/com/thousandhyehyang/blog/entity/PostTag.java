package com.thousandhyehyang.blog.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "post_tags")
public class PostTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "tag", nullable = false)
    private String tag;

    protected PostTag() {
    }

    public PostTag(Post post, String tag) {
        this.post = post;
        this.tag = tag;
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public String getTag() {
        return tag;
    }
}