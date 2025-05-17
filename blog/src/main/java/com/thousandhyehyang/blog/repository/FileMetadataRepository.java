package com.thousandhyehyang.blog.repository;

import com.thousandhyehyang.blog.entity.FileMetadata;
import com.thousandhyehyang.blog.enums.UploadType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    /**
     * 저장소 키로 파일 메타데이터 찾기
     */
    Optional<FileMetadata> findByStorageKey(String storageKey);

    /**
     * 원본 파일명으로 모든 파일 메타데이터 찾기
     */
    List<FileMetadata> findByOriginalFilename(String originalFilename);

    /**
     * 업로드 타입으로 모든 파일 메타데이터 찾기
     */
    List<FileMetadata> findByUploadType(UploadType uploadType);

    /**
     * 이전 버전 ID로 모든 파일 메타데이터 찾기
     */
    List<FileMetadata> findByPreviousVersionId(Long previousVersionId);

    /**
     * 원본 파일명으로 파일의 최신 버전 찾기
     */
    Optional<FileMetadata> findTopByOriginalFilenameOrderByVersionDesc(String originalFilename);

    /**
     * 공개 URL로 파일 메타데이터 찾기
     */
    Optional<FileMetadata> findByPublicUrl(String publicUrl);
}
