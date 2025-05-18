package com.thousandhyehyang.blog.service;

import com.thousandhyehyang.blog.dto.PostCreateRequest;
import com.thousandhyehyang.blog.dto.PostDetailResponse;
import com.thousandhyehyang.blog.dto.PostSummaryResponse;
import com.thousandhyehyang.blog.entity.Account;
import com.thousandhyehyang.blog.entity.FileMetadata;
import com.thousandhyehyang.blog.entity.Post;
import com.thousandhyehyang.blog.entity.PostFileMapping;
import com.thousandhyehyang.blog.exception.AuthenticationException;
import com.thousandhyehyang.blog.exception.DuplicateFileMappingException;
import com.thousandhyehyang.blog.exception.PostNotFoundException;
import com.thousandhyehyang.blog.repository.FileMetadataRepository;
import com.thousandhyehyang.blog.repository.PostFileMappingRepository;
import com.thousandhyehyang.blog.repository.PostRepository;
import com.thousandhyehyang.blog.util.HtmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

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

    /**
     * 현재 인증된 사용자 정보 가져오기
     * Spring Security의 SecurityContext에서 현재 인증된 사용자 정보를 추출합니다.
     * 
     * @return 인증된 사용자의 닉네임
     * @throws AuthenticationException 인증된 사용자 정보가 없거나 유효하지 않은 경우
     */
    private String getCurrentUserNickname() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AuthenticationException("인증 정보가 존재하지 않습니다.");
        }

        if (!(authentication.getPrincipal() instanceof Account)) {
            throw new AuthenticationException("유효하지 않은 인증 정보입니다.");
        }

        Account account = (Account) authentication.getPrincipal();
        return account.getNickname();
    }

    /**
     * 새로운 게시글 생성
     * 인증된 사용자의 정보를 바탕으로 게시글을 생성하고, 관련 파일들을 연결합니다.
     *
     * @param request 게시글 생성 요청 정보
     * @return 생성된 게시글의 ID
     * @throws AuthenticationException 인증된 사용자 정보가 없는 경우
     */
    @Transactional
    public Long create(PostCreateRequest request) {
        // 인증된 사용자의 닉네임을 작성자로 사용
        String author = getCurrentUserNickname();

        // 게시글 생성 및 저장
        Post post = createPostEntity(request, author);
        Post savedPost = postRepository.save(post);

        // 파일 연결 처리
        processPostFiles(savedPost, request);

        return savedPost.getId();
    }

    /**
     * 게시글 엔티티 생성
     * 요청 정보와 작성자 정보를 바탕으로 게시글 엔티티를 생성합니다.
     *
     * @param request 게시글 생성 요청 정보
     * @param author 게시글 작성자
     * @return 생성된 게시글 엔티티
     */
    private Post createPostEntity(PostCreateRequest request, String author) {
        // 게시글 기본 정보로 엔티티 생성
        Post post = new Post(
                request.title(),
                request.category(),
                request.content(),
                request.html(),
                request.thumbnailUrl(),
                author
        );

        // 태그 처리 로직
        processTags(post, request.tags());

        return post;
    }

    /**
     * 게시글 태그 처리
     * 요청에 포함된 태그 목록을 필터링하고 게시글에 추가합니다.
     *
     * @param post 태그를 추가할 게시글
     * @param tags 태그 목록
     */
    private void processTags(Post post, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }

        tags.stream()
                .filter(this::isValidTag)  // 유효한 태그만 필터링
                .distinct()                // 중복 제거
                .forEach(post::addTag);    // 태그 추가
    }

    /**
     * 태그 유효성 검사
     * 태그가 null이 아니고 비어있지 않은지 확인합니다.
     *
     * @param tag 검사할 태그
     * @return 유효한 태그인 경우 true
     */
    private boolean isValidTag(String tag) {
        return tag != null && !tag.isEmpty();
    }

    /**
     * 게시글 파일 처리
     * 게시글과 관련된 파일들(썸네일, 본문 내 이미지 등)을 연결합니다.
     *
     * @param post 파일을 연결할 게시글
     * @param request 게시글 생성 요청 정보
     */
    private void processPostFiles(Post post, PostCreateRequest request) {
        // 썸네일이 있는 경우 게시글과 연결
        processThumbnail(post, request.thumbnailUrl());

        // HTML 콘텐츠에서 파일 추출 및 연결
        associateFilesFromHtml(post, request.html());
    }

    /**
     * 썸네일 처리
     * 썸네일 URL이 있는 경우 게시글과 연결합니다.
     *
     * @param post 썸네일을 연결할 게시글
     * @param thumbnailUrl 썸네일 URL
     */
    private void processThumbnail(Post post, String thumbnailUrl) {
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            associateFileWithPost(post, thumbnailUrl, "THUMBNAIL");
        }
    }

    /**
     * 파일을 게시글과 연결
     * 파일 URL을 기반으로 파일 메타데이터를 찾아 게시글과 연결합니다.
     * 이미 연결된 파일인 경우 중복 매핑 예외를 발생시킵니다.
     *
     * @param post          파일과 연결할 게시글
     * @param fileUrl       파일의 URL
     * @param referenceType 참조 유형 (예: "THUMBNAIL", "IMAGE", "VIDEO", "DOCUMENT")
     * @throws DuplicateFileMappingException 이미 동일한 파일이 동일한 참조 유형으로 연결된 경우
     */
    private void associateFileWithPost(Post post, String fileUrl, String referenceType) {
        // 파일 URL로 메타데이터 조회
        Optional<FileMetadata> fileMetadataOpt = fileMetadataRepository.findByPublicUrl(fileUrl);

        if (!fileMetadataOpt.isPresent()) {
            log.warn("파일을 찾을 수 없음: URL={}, 게시글 ID={}", fileUrl, post.getId());
            return;
        }

        FileMetadata fileMetadata = fileMetadataOpt.get();

        // 중복 매핑 확인 및 예외 처리
        checkDuplicateFileMapping(post, fileMetadata, referenceType, fileUrl);

        // 게시글-파일 매핑 생성 및 저장
        createAndSaveFileMapping(post, fileMetadata, referenceType);
    }

    /**
     * 중복 파일 매핑 확인
     * 이미 동일한 파일이 동일한 참조 유형으로 연결되어 있는지 확인합니다.
     *
     * @param post 게시글
     * @param fileMetadata 파일 메타데이터
     * @param referenceType 참조 유형
     * @param fileUrl 파일 URL (로깅용)
     * @throws DuplicateFileMappingException 중복 매핑이 발견된 경우
     */
    private void checkDuplicateFileMapping(Post post, FileMetadata fileMetadata, String referenceType, String fileUrl) {
        if (postFileMappingRepository.existsByPostAndFileAndReferenceType(post, fileMetadata, referenceType)) {
            log.debug("중복 파일 매핑 감지: 게시글_ID={}, 파일_URL={}, 참조_유형={}", 
                    post.getId(), fileUrl, referenceType);
            throw new DuplicateFileMappingException(post.getId(), fileMetadata.getId(), referenceType);
        }
    }

    /**
     * 파일 매핑 생성 및 저장
     * 게시글과 파일 간의 매핑을 생성하고 저장합니다.
     *
     * @param post 게시글
     * @param fileMetadata 파일 메타데이터
     * @param referenceType 참조 유형
     */
    private void createAndSaveFileMapping(Post post, FileMetadata fileMetadata, String referenceType) {
        PostFileMapping mapping = new PostFileMapping(post, fileMetadata, referenceType);
        postFileMappingRepository.save(mapping);
        log.debug("파일이 게시글과 연결됨: 게시글_ID={}, 파일_ID={}, 참조_유형={}", 
                post.getId(), fileMetadata.getId(), referenceType);
    }

    /**
     * HTML 콘텐츠에서 파일을 추출하여 게시글과 연결
     * HTML 내용을 분석하여 포함된 미디어 파일(이미지, 비디오, 문서)을 찾아 게시글과 연결합니다.
     *
     * @param post 파일과 연결할 게시글
     * @param html 파일을 추출할 HTML 콘텐츠
     */
    private void associateFilesFromHtml(Post post, String html) {
        if (html == null || html.isEmpty()) {
            return;
        }

        // HTML에서 미디어 URL 추출
        Map<String, List<String>> mediaUrls = HtmlParser.extractMediaUrls(html);

        // TODO: 성능 개선을 위한 비동기 처리 구현
        // HTML에 수백 개의 파일 URL이 포함된 경우 성능 저하가 발생할 수 있음
        // 아래 코드를 CompletableFuture 또는 Spring의 @Async를 사용하여 비동기적으로 처리하도록 개선 필요
        // 예시: CompletableFuture.runAsync(() -> processMediaUrls(post, mediaUrls));

        processMediaUrls(post, mediaUrls);
    }

    /**
     * 미디어 URL을 처리하여 게시글과 연결
     * 추출된 미디어 URL을 유형별로 처리하여 게시글과 연결합니다.
     * 향후 비동기 처리를 위해 별도 메서드로 분리되었습니다.
     *
     * @param post 파일과 연결할 게시글
     * @param mediaUrls 처리할 미디어 URL 맵 (키: 미디어 유형, 값: URL 목록)
     */
    private void processMediaUrls(Post post, Map<String, List<String>> mediaUrls) {
        // 각 미디어 유형별로 처리
        processMediaUrlsByType(post, mediaUrls.get("IMAGE"), "IMAGE");
        processMediaUrlsByType(post, mediaUrls.get("VIDEO"), "VIDEO");
        processMediaUrlsByType(post, mediaUrls.get("DOCUMENT"), "DOCUMENT");
    }

    /**
     * 특정 유형의 미디어 URL 목록 처리
     * 동일한 유형의 미디어 URL 목록을 처리하여 게시글과 연결합니다.
     *
     * @param post 게시글
     * @param urls URL 목록
     * @param mediaType 미디어 유형
     */
    private void processMediaUrlsByType(Post post, List<String> urls, String mediaType) {
        if (urls == null || urls.isEmpty()) {
            return;
        }

        for (String url : urls) {
            if (url != null && !url.isEmpty()) {
                associateFileWithPost(post, url, mediaType);
            }
        }
    }

    /**
     * 최근 게시글 목록 조회
     * 최근에 작성된 게시글 10개를 조회하여 요약 정보로 반환합니다.
     *
     * @return 최근 게시글 10개의 요약 정보 목록
     */
    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getRecentPosts() {
        // 최근 게시글 10개 조회 후 DTO로 변환
        return postRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(PostSummaryResponse::from)
                .toList();
    }

    /**
     * ID로 게시글 조회
     * 주어진 ID로 게시글을 조회하고, 존재하지 않는 경우 예외를 발생시킵니다.
     *
     * @param id 조회할 게시글의 ID
     * @return 게시글 엔티티
     * @throws PostNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        // ID로 게시글 조회, 없으면 예외 발생
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    /**
     * ID로 게시글 상세 정보 조회
     * 게시글 정보와 연결된 파일 정보를 함께 조회하여 상세 정보로 반환합니다.
     *
     * @param id 조회할 게시글의 ID
     * @return 게시글 상세 정보 응답 객체
     * @throws PostNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(Long id) {
        // 게시글 조회
        Post post = getPostById(id);

        // 게시글과 연결된 파일 매핑 조회
        List<PostFileMapping> fileMappings = postFileMappingRepository.findByPost(post);

        // 게시글과 파일 정보를 DTO로 변환하여 반환
        return PostDetailResponse.from(post, fileMappings);
    }

    /**
     * 게시글 삭제
     * 게시글을 소프트 삭제하고, 연결된 파일 매핑을 정리합니다.
     * 다른 게시글에서 사용하지 않는 파일은 삭제 대상으로 표시합니다.
     *
     * @param id 삭제할 게시글의 ID
     * @throws PostNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional
    public void deletePost(Long id) {
        // 게시글 조회
        Post post = getPostById(id);

        // 파일 매핑 정리 및 고아 파일 처리
        List<Long> orphanedFileIds = cleanupFileAssociations(post);

        // 게시글 삭제 (소프트 삭제)
        // @SQLDelete 어노테이션에 의해 실제로는 UPDATE 쿼리가 실행됨
        postRepository.delete(post);
        log.info("게시글 삭제 완료: ID={}", id);

        // 고아 파일 처리
        markOrphanedFilesForDeletion(orphanedFileIds);
    }

    /**
     * 파일 연결 정리
     * 게시글과 연결된 파일 매핑을 삭제하고, 고아가 된 파일 ID 목록을 반환합니다.
     *
     * @param post 삭제할 게시글
     * @return 다른 게시글에서 사용하지 않는 파일 ID 목록
     */
    private List<Long> cleanupFileAssociations(Post post) {
        // 게시글과 연결된 모든 파일 매핑 찾기
        List<PostFileMapping> fileMappings = postFileMappingRepository.findByPost(post);

        // 파일 ID 목록 추출
        List<Long> fileIds = fileMappings.stream()
                .map(mapping -> mapping.getFile().getId())
                .distinct()
                .toList();

        // 게시글과 연결된 파일 매핑 삭제
        postFileMappingRepository.deleteByPost(post);
        log.info("게시글 파일 매핑 삭제 완료: 게시글_ID={}, 매핑_수={}", post.getId(), fileMappings.size());

        // 다른 게시글에서 사용하지 않는 파일 ID 목록 반환
        return findOrphanedFileIds(fileIds);
    }

    /**
     * 고아 파일 ID 찾기
     * 주어진 파일 ID 목록 중 다른 게시글에서 사용하지 않는 파일 ID를 찾습니다.
     *
     * @param fileIds 확인할 파일 ID 목록
     * @return 다른 게시글에서 사용하지 않는 파일 ID 목록
     */
    private List<Long> findOrphanedFileIds(List<Long> fileIds) {
        return fileIds.stream()
                .filter(fileId -> postFileMappingRepository.findByFileId(fileId).isEmpty())
                .toList();
    }

    /**
     * 고아 파일 삭제 표시
     * 다른 게시글에서 사용하지 않는 파일을 삭제 대상으로 표시합니다.
     * 실제 파일 삭제는 별도의 배치 작업이나 스케줄러에서 처리합니다.
     *
     * @param orphanedFileIds 고아 파일 ID 목록
     */
    private void markOrphanedFilesForDeletion(List<Long> orphanedFileIds) {
        if (orphanedFileIds.isEmpty()) {
            return;
        }

        for (Long fileId : orphanedFileIds) {
            log.info("고아 파일 감지 (삭제 대상): 파일_ID={}", fileId);
            // TODO: 실제 파일 삭제 로직 구현 또는 삭제 대상으로 표시
            // 파일 삭제는 별도의 배치 작업이나 스케줄러에서 처리하는 것이 권장됨
            // fileMetadataRepository.deleteById(fileId);
        }
    }
}
