package com.thousandhyehyang.blog.controller;

import com.thousandhyehyang.blog.common.ApiResponse;
import com.thousandhyehyang.blog.dto.post.PostCreateRequest;
import com.thousandhyehyang.blog.dto.post.PostDetailResponse;
import com.thousandhyehyang.blog.dto.post.PostSummaryResponse;
import com.thousandhyehyang.blog.dto.post.PostUpdateRequest;
import com.thousandhyehyang.blog.service.post.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 게시글 컨트롤러
 * 게시글 생성, 조회, 삭제 등의 HTTP 요청을 처리합니다.
 * 모든 요청은 인증된 사용자만 처리할 수 있습니다.
 */
@RestController
@RequestMapping("/posts")
@Validated
@Tag(name = "게시글 API", description = "게시글 생성, 조회, 삭제 관련 API")
public class PostController {

    private final PostService postService;

    /**
     * 생성자를 통한 의존성 주입
     * 
     * @param postService 게시글 서비스
     */
    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * 게시글 생성 API
     * 인증된 사용자의 정보를 바탕으로 새로운 게시글을 생성합니다.
     * 
     * @param request 게시글 생성 요청 정보
     * @return 생성된 게시글의 ID
     */
    @Operation(
            summary = "게시글 생성",
            description = "새로운 게시글을 생성합니다. 본문에 포함된 이미지/영상/문서 파일들은 자동으로 게시글과 연결됩니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createPost(
            @Parameter(description = "게시글 생성 요청 정보", required = true)
            @Valid @RequestBody PostCreateRequest request
    ) {
        // 게시글 생성 및 ID 반환
        Long id = postService.create(request);
        return ResponseEntity.status(201).body(new ApiResponse<>(id));
    }

    /**
     * 최근 게시글 목록 조회 API
     * 최근에 작성된 게시글의 요약 정보를 조회합니다.
     * 
     * @param limit 조회할 게시글 수 (기본값: 10)
     * @return 최근 게시글의 요약 정보 목록
     */
    @Operation(
            summary = "최근 게시글 목록 조회",
            description = "최근에 작성된 게시글을 조회합니다. limit 파라미터로 조회할 게시글 수를 지정할 수 있습니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostSummaryResponse>>> getRecentPosts(
            @Parameter(description = "조회할 게시글 수 (기본값: 10)", example = "10")
            @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        // 최근 게시글 목록 조회 및 반환
        List<PostSummaryResponse> posts = postService.getRecentPosts(limit);
        return ResponseEntity.ok(new ApiResponse<>(posts));
    }

    /**
     * 임시저장 게시글 목록 조회 API
     * 현재 로그인한 사용자가 임시저장한 게시글의 요약 정보를 조회합니다.
     * 
     * @return 임시저장 게시글의 요약 정보 목록
     */
    @Operation(
            summary = "임시저장 게시글 목록 조회",
            description = "현재 로그인한 사용자가 임시저장한 게시글을 조회합니다."
    )
    @GetMapping("/drafts")
    public ResponseEntity<ApiResponse<List<PostSummaryResponse>>> getDraftPosts() {
        // 임시저장 게시글 목록 조회 및 반환
        List<PostSummaryResponse> draftPosts = postService.getDraftPosts();
        return ResponseEntity.ok(new ApiResponse<>(draftPosts));
    }

    /**
     * 게시글 상세 조회 API
     * 게시글 ID로 게시글의 상세 정보를 조회합니다.
     * 
     * @param id 조회할 게시글의 ID
     * @return 게시글 상세 정보
     */
    @Operation(
            summary = "게시글 상세 조회",
            description = "게시글 ID로 게시글 상세 정보를 조회합니다. 게시글과 연결된 파일 정보도 함께 제공됩니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetail(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long id
    ) {
        // 게시글 상세 정보 조회 및 반환
        PostDetailResponse postDetail = postService.getPostDetail(id);
        return ResponseEntity.ok(new ApiResponse<>(postDetail));
    }

    /**
     * 게시글 수정 API
     * 게시글 ID로 게시글을 수정합니다.
     * 
     * @param id 수정할 게시글의 ID
     * @param request 게시글 수정 요청 정보
     * @return 수정된 게시글 상세 정보
     */
    @Operation(
            summary = "게시글 수정",
            description = "게시글 ID로 게시글을 수정합니다. 게시글과 연결된 파일 중 다른 게시글에서 사용하지 않는 파일도 함께 삭제됩니다."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> updatePost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "게시글 수정 요청 정보", required = true)
            @Valid @RequestBody PostUpdateRequest request
    ) {
        // 게시글 수정 및 상세 정보 반환
        PostDetailResponse updatedPost = postService.updatePost(id, request);
        return ResponseEntity.ok(new ApiResponse<>(updatedPost));
    }

    /**
     * 게시글 삭제 API
     * 게시글 ID로 게시글을 삭제합니다.
     * 
     * @param id 삭제할 게시글의 ID
     * @return 204 No Content 응답
     */
    @Operation(
            summary = "게시글 삭제",
            description = "게시글 ID로 게시글을 삭제합니다. 게시글과 연결된 파일 중 다른 게시글에서 사용하지 않는 파일도 함께 삭제됩니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long id
    ) {
        // 게시글 삭제
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
