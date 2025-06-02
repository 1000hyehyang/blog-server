package com.thousandhyehyang.blog.service.comment;

import com.thousandhyehyang.blog.dto.comment.CommentCreateRequest;
import com.thousandhyehyang.blog.dto.comment.CommentResponse;
import com.thousandhyehyang.blog.dto.comment.CommentUpdateRequest;
import com.thousandhyehyang.blog.entity.Comment;
import com.thousandhyehyang.blog.entity.Post;
import com.thousandhyehyang.blog.exception.CommentNotFoundException;
import com.thousandhyehyang.blog.exception.PostNotFoundException;
import com.thousandhyehyang.blog.repository.CommentRepository;
import com.thousandhyehyang.blog.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    /**
     * 게시글에 댓글 생성
     *
     * @param postId  게시글 ID
     * @param request 댓글 생성 요청
     * @return 생성된 댓글 정보
     * @throws PostNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional
    public CommentResponse createComment(Long postId, CommentCreateRequest request) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId));

        // 댓글 엔티티 생성 및 저장
        Comment comment = new Comment(post, request.getNickname(), request.getContent(), request.getEmoji(), request.getBgColor());
        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.from(savedComment);
    }

    /**
     * 게시글의 댓글 목록 조회
     *
     * @param postId 게시글 ID
     * @return 댓글 목록
     * @throws PostNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        // 게시글 존재 여부 확인
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId);
        }

        // 댓글 목록 조회 및 DTO 변환
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId).stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 댓글 수정
     *
     * @param postId 게시글 ID
     * @param commentId 댓글 ID
     * @param request 댓글 수정 요청
     * @return 수정된 댓글 정보
     * @throws PostNotFoundException 게시글을 찾을 수 없는 경우
     * @throws CommentNotFoundException 댓글을 찾을 수 없는 경우
     * @throws IllegalArgumentException 댓글이 해당 게시글에 속하지 않는 경우
     */
    @Transactional
    public CommentResponse updateComment(Long postId, Long commentId, CommentUpdateRequest request) {
        // 게시글 존재 여부 확인
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId);
        }

        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다. ID: " + commentId));

        // 댓글이 해당 게시글에 속하는지 확인
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글에 속한 댓글이 아닙니다. 게시글 ID: " + postId + ", 댓글 ID: " + commentId);
        }

        // 댓글 업데이트
        comment.update(
                request.getNickname(),
                request.getContent(),
                request.getEmoji(),
                request.getBgColor()
        );

        // 변경 사항 저장
        Comment updatedComment = commentRepository.save(comment);

        return CommentResponse.from(updatedComment);
    }

    /**
     * 댓글 삭제
     *
     * @param postId 게시글 ID
     * @param commentId 댓글 ID
     * @throws PostNotFoundException 게시글을 찾을 수 없는 경우
     * @throws CommentNotFoundException 댓글을 찾을 수 없는 경우
     * @throws IllegalArgumentException 댓글이 해당 게시글에 속하지 않는 경우
     */
    @Transactional
    public void deleteComment(Long postId, Long commentId) {
        // 게시글 존재 여부 확인
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("게시글을 찾을 수 없습니다. ID: " + postId);
        }

        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다. ID: " + commentId));

        // 댓글이 해당 게시글에 속하는지 확인
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글에 속한 댓글이 아닙니다. 게시글 ID: " + postId + ", 댓글 ID: " + commentId);
        }

        // 댓글 삭제
        commentRepository.deleteById(commentId);
    }
}
