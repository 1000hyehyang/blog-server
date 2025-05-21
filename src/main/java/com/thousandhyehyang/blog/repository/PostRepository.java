package com.thousandhyehyang.blog.repository;

import com.thousandhyehyang.blog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findTop10ByOrderByCreatedAtDesc();

    List<Post> findByOrderByCreatedAtDesc();

    @org.springframework.data.jpa.repository.Query(value = "SELECT p FROM Post p ORDER BY p.createdAt DESC")
    List<Post> findRecentPosts(org.springframework.data.domain.Pageable pageable);

    // 임시저장 게시글 조회 메서드
    List<Post> findByAuthorAndDraftIsTrue(String author);

    // 작성자별 임시저장 게시글 조회 (최신순)
    List<Post> findByAuthorAndDraftTrueOrderByCreatedAtDesc(String author);

    // 작성자별 정식 게시글 조회 (최신순)
    List<Post> findByAuthorAndDraftIsFalseOrderByCreatedAtDesc(String author);

    /**
     * ID로 게시글을 조회하면서 태그 정보를 함께 가져오는 메서드 (N+1 문제 해결)
     * 
     * @param id 게시글 ID
     * @return 태그 정보가 포함된 게시글
     */
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Post p LEFT JOIN FETCH p.postTags WHERE p.id = :id")
    java.util.Optional<Post> findByIdWithTags(@org.springframework.data.repository.query.Param("id") Long id);
}
