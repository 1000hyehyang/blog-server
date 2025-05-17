package com.thousandhyehyang.blog.service;

import com.thousandhyehyang.blog.dto.PostCreateRequest;
import com.thousandhyehyang.blog.dto.PostDetailResponse;
import com.thousandhyehyang.blog.dto.PostSummaryResponse;
import com.thousandhyehyang.blog.entity.FileMetadata;
import com.thousandhyehyang.blog.entity.Post;
import com.thousandhyehyang.blog.entity.PostFileMapping;
import com.thousandhyehyang.blog.repository.FileMetadataRepository;
import com.thousandhyehyang.blog.repository.PostFileMappingRepository;
import com.thousandhyehyang.blog.repository.PostRepository;
import com.thousandhyehyang.blog.util.HtmlParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final PostFileMappingRepository postFileMappingRepository;

    public PostService(PostRepository postRepository, 
                      FileMetadataRepository fileMetadataRepository,
                      PostFileMappingRepository postFileMappingRepository) {
        this.postRepository = postRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.postFileMappingRepository = postFileMappingRepository;
    }

    @Transactional
    public Long create(PostCreateRequest request, String author) {
        // 게시글 생성 및 저장
        Post post = new Post(
                request.title(),
                request.category(),
                request.content(),
                request.html(),
                request.thumbnailUrl(),
                author
        );

        // 태그 처리 로직 (비즈니스 로직을 서비스 계층으로 이동)
        if (request.tags() != null) {
            request.tags().stream()
                    .filter(tag -> tag != null && !tag.isEmpty())  // 빈 문자열 제거
                    .distinct()                                    // 중복 제거
                    .forEach(post::addTag);                        // 태그 추가
        }

        Post savedPost = postRepository.save(post);

        // 썸네일이 있는 경우 게시글과 연결
        if (request.thumbnailUrl() != null && !request.thumbnailUrl().isEmpty()) {
            associateFileWithPost(savedPost, request.thumbnailUrl(), "THUMBNAIL");
        }

        // HTML 콘텐츠에서 파일 추출 및 연결
        associateFilesFromHtml(savedPost, request.html());

        return savedPost.getId();
    }

    /**
     * 파일을 게시글과 연결
     * 
     * @param post 파일과 연결할 게시글
     * @param fileUrl 파일의 URL
     * @param referenceType 참조 유형 (예: "THUMBNAIL", "CONTENT")
     */
    private void associateFileWithPost(Post post, String fileUrl, String referenceType) {
        Optional<FileMetadata> fileMetadata = fileMetadataRepository.findByPublicUrl(fileUrl);

        if (fileMetadata.isPresent()) {
            PostFileMapping mapping = new PostFileMapping(post, fileMetadata.get(), referenceType);
            postFileMappingRepository.save(mapping);
        }
    }

    /**
     * HTML 콘텐츠에서 파일을 추출하여 게시글과 연결
     * 
     * @param post 파일과 연결할 게시글
     * @param html 파일을 추출할 HTML 콘텐츠
     */
    private void associateFilesFromHtml(Post post, String html) {
        Map<String, List<String>> mediaUrls = HtmlParser.extractMediaUrls(html);

        // 이미지 파일 연결
        for (String imageUrl : mediaUrls.get("IMAGE")) {
            associateFileWithPost(post, imageUrl, "IMAGE");
        }

        // 비디오 파일 연결
        for (String videoUrl : mediaUrls.get("VIDEO")) {
            associateFileWithPost(post, videoUrl, "VIDEO");
        }

        // 문서 파일 연결
        for (String documentUrl : mediaUrls.get("DOCUMENT")) {
            associateFileWithPost(post, documentUrl, "DOCUMENT");
        }
    }

    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getRecentPosts() {
        return postRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(PostSummaryResponse::from)
                .toList();
    }

    /**
     * ID로 게시글 조회
     * 
     * @param id 조회할 게시글의 ID
     * @return 게시글
     * @throws IllegalArgumentException 게시글을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with ID: " + id));
    }

    /**
     * ID로 게시글 상세 정보 조회
     * 
     * @param id 조회할 게시글의 ID
     * @return 게시글 상세 정보 응답
     * @throws IllegalArgumentException 게시글을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(Long id) {
        Post post = getPostById(id);
        List<PostFileMapping> fileMappings = postFileMappingRepository.findByPost(post);
        return PostDetailResponse.from(post, fileMappings);
    }

    /**
     * 게시글 소프트 삭제
     * 
     * @param id 삭제할 게시글의 ID
     * @throws IllegalArgumentException 게시글을 찾을 수 없는 경우
     */
    @Transactional
    public void deletePost(Long id) {
        Post post = getPostById(id);

        // @SQLDelete 어노테이션에 의해 실제로는 UPDATE 쿼리가 실행됨
        postRepository.delete(post);

        // 참고: 파일 매핑과 파일은 그대로 유지됩니다.
        // 게시글이 실제로 삭제되지 않기 때문에 연결된 파일도 유지됩니다.
    }
}
