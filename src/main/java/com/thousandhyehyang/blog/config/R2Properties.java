package com.thousandhyehyang.blog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloudflare.r2")
public class R2Properties {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String endpoint;
    private String publicUrl;
    private String region;

    // 기본값이 있는 파일 업로드 경로
    private String thumbnailPath = "thumbnails/";
    private String editorImagePath = "editor-images/";
    private String editorVideoPath = "editor-videos/";
    private String documentPath = "documents/";

    // 게터 & 세터
    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getPublicUrl() { return publicUrl; }
    public void setPublicUrl(String publicUrl) { this.publicUrl = publicUrl; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

    public String getEditorImagePath() { return editorImagePath; }
    public void setEditorImagePath(String editorImagePath) { this.editorImagePath = editorImagePath; }

    public String getEditorVideoPath() { return editorVideoPath; }
    public void setEditorVideoPath(String editorVideoPath) { this.editorVideoPath = editorVideoPath; }

    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
}
