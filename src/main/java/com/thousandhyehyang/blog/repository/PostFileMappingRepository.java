package com.thousandhyehyang.blog.repository;

import com.thousandhyehyang.blog.entity.FileMetadata;
import com.thousandhyehyang.blog.entity.Post;
import com.thousandhyehyang.blog.entity.PostFileMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostFileMappingRepository extends JpaRepository<PostFileMapping, Long> {

    /**
     * 특정 게시글의 모든 파일 매핑 찾기
     */
    List<PostFileMapping> findByPost(Post post);

    /**
     * 특정 게시글 ID의 모든 파일 매핑 찾기
     */
    List<PostFileMapping> findByPostId(Long postId);

    /**
     * 특정 파일의 모든 파일 매핑 찾기
     */
    List<PostFileMapping> findByFile(FileMetadata file);

    /**
     * 특정 파일 ID의 모든 파일 매핑 찾기
     */
    List<PostFileMapping> findByFileId(Long fileId);

    /**
     * 게시글과 파일로 특정 매핑 찾기
     */
    Optional<PostFileMapping> findByPostAndFile(Post post, FileMetadata file);

    /**
     * 게시글, 파일, 참조 유형으로 특정 매핑 찾기
     */
    Optional<PostFileMapping> findByPostAndFileAndReferenceType(Post post, FileMetadata file, String referenceType);

    /**
     * 게시글, 파일, 참조 유형으로 매핑 존재 여부 확인
     */
    boolean existsByPostAndFileAndReferenceType(Post post, FileMetadata file, String referenceType);

    /**
     * 게시글과 참조 유형으로 모든 매핑 찾기
     */
    List<PostFileMapping> findByPostAndReferenceType(Post post, String referenceType);

    /**
     * 특정 게시글의 모든 매핑 삭제
     */
    void deleteByPost(Post post);

    /**
     * 특정 파일의 모든 매핑 삭제
     */
    void deleteByFile(FileMetadata file);
}
