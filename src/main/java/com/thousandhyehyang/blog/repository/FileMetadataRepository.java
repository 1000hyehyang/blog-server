package com.thousandhyehyang.blog.repository;

import com.thousandhyehyang.blog.entity.FileMetadata;
import com.thousandhyehyang.blog.enums.UploadType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    /**
     * 고아 파일 찾기 (어떤 게시글에도 연결되지 않은 파일)
     * 특정 날짜 이전에 생성되었으나 어떤 게시글에도 연결되지 않은 파일 목록을 조회합니다.
     * 
     * @param cutoffDate 기준 날짜 (이 날짜 이전에 생성된 파일만 대상)
     * @return 고아 파일 목록
     */
    @Query("SELECT f FROM FileMetadata f WHERE f.id NOT IN " +
           "(SELECT DISTINCT pfm.file.id FROM PostFileMapping pfm) " +
           "AND f.createdAt < :cutoffDate")
    List<FileMetadata> findOrphanedFiles(@Param("cutoffDate") LocalDateTime cutoffDate);
}
