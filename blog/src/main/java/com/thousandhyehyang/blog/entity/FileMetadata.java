package com.thousandhyehyang.blog.entity;

import com.thousandhyehyang.blog.enums.UploadType;
import jakarta.persistence.*;

@Entity
@Table(name = "file_metadata")
public class FileMetadata extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false, unique = true)
    private String storageKey;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UploadType uploadType;

    @Column(nullable = false)
    private String publicUrl;

    @Column(nullable = false)
    private Integer version = 1;

    // 버전 관리용
    @Column
    private Long previousVersionId;

    // JPA용 기본 생성자
    protected FileMetadata() {
    }

    // 모든 필수 필드를 포함한 생성자
    public FileMetadata(String originalFilename, String storageKey, String contentType, 
                        Long fileSize, UploadType uploadType, String publicUrl) {
        this.originalFilename = originalFilename;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.uploadType = uploadType;
        this.publicUrl = publicUrl;
    }

    // 게터와 세터
    public Long getId() {
        return id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public UploadType getUploadType() {
        return uploadType;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getPreviousVersionId() {
        return previousVersionId;
    }

    public void setPreviousVersionId(Long previousVersionId) {
        this.previousVersionId = previousVersionId;
    }
}
