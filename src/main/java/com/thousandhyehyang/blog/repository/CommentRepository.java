package com.thousandhyehyang.blog.repository;

import com.thousandhyehyang.blog.entity.Comment;
import com.thousandhyehyang.blog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);
}