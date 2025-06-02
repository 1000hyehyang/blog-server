package com.thousandhyehyang.blog.controller;

import com.thousandhyehyang.blog.common.ApiResponse;
import com.thousandhyehyang.blog.dto.comment.CommentCreateRequest;
import com.thousandhyehyang.blog.dto.comment.CommentResponse;
import com.thousandhyehyang.blog.dto.comment.CommentUpdateRequest;
import com.thousandhyehyang.blog.service.comment.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 댓글 컨트롤러
 * 댓글 생성, 조회, 수정, 삭제 등의 HTTP 요청을 처리합니다.
 */
@RestController
@Validated
@Tag(name = "댓글 API", description = "댓글 생성, 조회, 수정, 삭제 관련 API")
public class CommentController {

    private final CommentService commentService;

    /**
     * 생성자를 통한 의존성 주입
     * 
     * @param commentService 댓글 서비스
     */
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 댓글 조회 API
     * 게시글의 댓글 목록을 조회합니다.
     * 
     * @param postId 게시글 ID
     * @return 댓글 목록
     */
    @Operation(
            summary = "게시글의 댓글 목록 조회",
            description = "게시글의 댓글 목록을 조회합니다. 최신 댓글이 먼저 표시됩니다."
    )
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByPostId(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId
    ) {
        // 댓글 목록 조회 및 반환
        List<CommentResponse> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(new ApiResponse<>(comments));
    }

    /**
     * 댓글 생성 API
     * 게시글에 익명 댓글을 작성합니다.
     * 
     * @param postId 게시글 ID
     * @param request 댓글 생성 요청 정보
     * @return 생성된 댓글 정보
     */
    @Operation(
            summary = "댓글 생성",
            description = "게시글에 익명 댓글을 작성합니다. 닉네임, 내용, 이모지, 배경색을 입력해야 합니다."
    )
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId,
            @Parameter(description = "댓글 생성 요청 정보", required = true)
            @Valid @RequestBody CommentCreateRequest request
    ) {
        // 댓글 생성 및 반환
        CommentResponse comment = commentService.createComment(postId, request);
        return ResponseEntity.status(201).body(new ApiResponse<>(comment));
    }

    /**
     * 댓글 수정 API
     * 댓글을 수정합니다.
     * 
     * @param commentId 댓글 ID
     * @param request 댓글 수정 요청 정보
     * @return 수정된 댓글 정보
     */
    @Operation(
            summary = "댓글 수정",
            description = "댓글을 수정합니다. 닉네임, 내용, 이모지, 배경색 중 변경할 항목만 전송하면 됩니다."
    )
    @PutMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId,
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable Long commentId,
            @Parameter(description = "댓글 수정 요청 정보", required = true)
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        // 댓글 수정 및 반환
        CommentResponse updatedComment = commentService.updateComment(postId, commentId, request);
        return ResponseEntity.ok(new ApiResponse<>(updatedComment));
    }

    /**
     * 댓글 삭제 API
     * 댓글을 삭제합니다.
     * 
     * @param commentId 댓글 ID
     * @return 204 No Content 응답
     */
    @Operation(
            summary = "댓글 삭제",
            description = "댓글을 삭제합니다."
    )
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId,
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable Long commentId
    ) {
        // 댓글 삭제
        commentService.deleteComment(postId, commentId);
        return ResponseEntity.noContent().build();
    }
}
