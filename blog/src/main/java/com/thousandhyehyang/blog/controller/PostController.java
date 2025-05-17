package com.thousandhyehyang.blog.controller;

import com.thousandhyehyang.blog.common.ApiResponse;
import com.thousandhyehyang.blog.dto.PostCreateRequest;
import com.thousandhyehyang.blog.dto.PostDetailResponse;
import com.thousandhyehyang.blog.dto.PostSummaryResponse;
import com.thousandhyehyang.blog.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@Validated
@Tag(name = "게시글 API", description = "게시글 생성, 조회, 삭제 관련 API")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(
        summary = "게시글 생성",
        description = "새로운 게시글을 생성합니다. 본문에 포함된 이미지/영상/문서 파일들은 자동으로 게시글과 연결됩니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createPost(
            @Parameter(description = "게시글 생성 요청 정보", required = true)
            @Valid @RequestBody PostCreateRequest request,
            @Parameter(description = "작성자", required = true)
            @RequestParam String author
    ) {
        Long id = postService.create(request, author);
        return ResponseEntity.status(201).body(new ApiResponse<>(id));
    }

    @Operation(
        summary = "최근 게시글 목록 조회",
        description = "최근에 작성된 게시글 10개를 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostSummaryResponse>>> getRecentPosts() {
        return ResponseEntity.ok(new ApiResponse<>(postService.getRecentPosts()));
    }

    @Operation(
        summary = "게시글 상세 조회",
        description = "게시글 ID로 게시글 상세 정보를 조회합니다. 게시글과 연결된 파일 정보도 함께 제공됩니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetail(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(new ApiResponse<>(postService.getPostDetail(id)));
    }

    @Operation(
        summary = "게시글 삭제",
        description = "게시글 ID로 게시글을 삭제합니다. 게시글과 연결된 파일 중 다른 게시글에서 사용하지 않는 파일도 함께 삭제됩니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long id
    ) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
