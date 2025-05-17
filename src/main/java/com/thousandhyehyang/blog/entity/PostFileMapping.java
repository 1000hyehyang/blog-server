package com.thousandhyehyang.blog.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "post_file_mappings")
public class PostFileMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileMetadata file;

    // 참조 유형 (예: "THUMBNAIL", "CONTENT")
    @Column(nullable = false)
    private String referenceType;

    protected PostFileMapping() {
    }

    public PostFileMapping(Post post, FileMetadata file, String referenceType) {
        this.post = post;
        this.file = file;
        this.referenceType = referenceType;
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public FileMetadata getFile() {
        return file;
    }

    public String getReferenceType() {
        return referenceType;
    }
}
