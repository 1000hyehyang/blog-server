package com.thousandhyehyang.blog.service.post;

import com.thousandhyehyang.blog.dto.post.PostCreateRequest;
import com.thousandhyehyang.blog.dto.post.PostDetailResponse;
import com.thousandhyehyang.blog.dto.post.PostSummaryResponse;
import com.thousandhyehyang.blog.dto.post.PostUpdateRequest;
import com.thousandhyehyang.blog.entity.Post;
import com.thousandhyehyang.blog.entity.PostFileMapping;
import com.thousandhyehyang.blog.exception.AuthenticationException;
import com.thousandhyehyang.blog.exception.PostNotFoundException;
import com.thousandhyehyang.blog.repository.PostFileMappingRepository;
import com.thousandhyehyang.blog.repository.PostRepository;
import com.thousandhyehyang.blog.service.email.EmailService;
import com.thousandhyehyang.blog.service.file.MediaProcessorService;
import com.thousandhyehyang.blog.service.file.ThumbnailService;
import com.thousandhyehyang.blog.util.HtmlParser;
import com.thousandhyehyang.blog.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final PostFileMappingRepository postFileMappingRepository;
    private final SecurityUtil securityUtil;
    private final TagService tagService;
    private final MediaProcessorService mediaProcessorService;
    private final ThumbnailService thumbnailService;
    private final EmailService emailService;

    public PostService(PostRepository postRepository,
                       PostFileMappingRepository postFileMappingRepository,
                       SecurityUtil securityUtil,
                       TagService tagService,
                       MediaProcessorService mediaProcessorService,
                       ThumbnailService thumbnailService,
                       EmailService emailService) {
        this.postRepository = postRepository;
        this.postFileMappingRepository = postFileMappingRepository;
        this.securityUtil = securityUtil;
        this.tagService = tagService;
        this.mediaProcessorService = mediaProcessorService;
        this.thumbnailService = thumbnailService;
        this.emailService = emailService;
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
        String author = securityUtil.getCurrentUserNickname();

        // 게시글 생성 및 저장
        Post post = createPostEntity(request, author);
        Post savedPost = postRepository.save(post);

        // 파일 연결 처리
        processPostFiles(savedPost, request);

        // 임시저장이 아닌 경우에만 이메일 알림 발송
        if (!savedPost.isDraft()) {
            emailService.sendNewPostNotification(savedPost);
        }

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
        // HTML에서 텍스트 추출 (최대 200자)
        String extractedContent = HtmlParser.extractText(request.html());

        // 게시글 기본 정보로 엔티티 생성
        Post post = new Post(
                request.title(),
                request.category(),
                extractedContent, // HTML에서 추출한 텍스트를 content로 사용
                request.html(),
                request.thumbnailUrl(),
                author,
                request.draft()
        );

        // 태그 처리 로직
        tagService.processTags(post, request.tags());

        return post;
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
        thumbnailService.processThumbnail(post, request.thumbnailUrl());

        // HTML 콘텐츠에서 파일 추출 및 연결
        mediaProcessorService.associateFilesFromHtml(post, request.html());
    }

    /**
     * 최근 게시글 목록 조회
     * 최근에 작성된 게시글을 조회하여 요약 정보로 반환합니다.
     *
     * @param limit 조회할 게시글 수 (기본값: 10)
     * @return 최근 게시글의 요약 정보 목록
     */
    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getRecentPosts(int limit) {
        // 최근 게시글 조회 후 DTO로 변환 (임시저장이 아닌 게시글만)
        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findRecentPosts(pageable)
                .stream()
                .filter(post -> !post.isDraft())
                .map(PostSummaryResponse::from)
                .toList();
    }

    /**
     * 사용자의 임시저장 게시글 목록 조회
     * 현재 로그인한 사용자가 임시저장한 게시글의 요약 정보를 조회합니다.
     *
     * @return 임시저장 게시글의 요약 정보 목록
     */
    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getDraftPosts() {
        // 현재 사용자 닉네임 가져오기
        String currentUserNickname = securityUtil.getCurrentUserNickname();

        // 사용자의 임시저장 게시글 조회
        List<Post> draftPosts = postRepository.findByAuthorAndDraftTrueOrderByCreatedAtDesc(currentUserNickname);

        // 게시글 엔티티를 DTO로 변환하여 반환
        return draftPosts.stream()
                .map(PostSummaryResponse::from)
                .toList();
    }

    /**
     * 최근 게시글 목록 조회 (기본 10개)
     * 최근에 작성된 게시글 10개를 조회하여 요약 정보로 반환합니다.
     *
     * @return 최근 게시글 10개의 요약 정보 목록
     */
    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getRecentPosts() {
        return getRecentPosts(10);
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
     * ID로 게시글 조회 (태그 정보 포함)
     * 주어진 ID로 게시글을 조회하고, 태그 정보를 함께 가져옵니다.
     * N+1 문제를 방지하기 위해 JOIN FETCH를 사용합니다.
     *
     * @param id 조회할 게시글의 ID
     * @return 태그 정보가 포함된 게시글 엔티티
     * @throws PostNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public Post getPostByIdWithTags(Long id) {
        // ID로 게시글과 태그 정보를 함께 조회, 없으면 예외 발생
        return postRepository.findByIdWithTags(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    /**
     * ID로 게시글 상세 정보 조회
     * 게시글 정보와 연결된 파일 정보를 함께 조회하여 상세 정보로 반환합니다.
     * 임시저장 게시글은 작성자만 조회할 수 있습니다.
     *
     * @param id 조회할 게시글의 ID
     * @return 게시글 상세 정보 응답 객체
     * @throws PostNotFoundException 게시글을 찾을 수 없는 경우
     * @throws AuthenticationException 임시저장 게시글에 대한 접근 권한이 없는 경우
     */
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(Long id) {
        // 게시글 조회 (태그 정보 포함)
        Post post = getPostByIdWithTags(id);

        // 임시저장 게시글인 경우 작성자 확인
        if (post.isDraft()) {
            String currentUserNickname = securityUtil.getCurrentUserNickname();
            if (!post.getAuthor().equals(currentUserNickname)) {
                throw new AuthenticationException("임시저장 게시글은 작성자만 조회할 수 있습니다.");
            }
        }

        // 게시글과 연결된 파일 매핑 조회
        List<PostFileMapping> fileMappings = postFileMappingRepository.findByPost(post);

        // 게시글과 파일 정보를 DTO로 변환하여 반환
        return PostDetailResponse.from(post, fileMappings);
    }

    /**
     * 게시글 수정
     * 게시글의 내용을 수정하고, 태그와 파일 연결을 업데이트합니다.
     *
     * @param id 수정할 게시글의 ID
     * @param request 게시글 수정 요청 정보
     * @return 수정된 게시글 상세 정보
     * @throws PostNotFoundException 게시글을 찾을 수 없는 경우
     * @throws AuthenticationException 인증되지 않은 사용자가 접근한 경우
     */
    @Transactional
    public PostDetailResponse updatePost(Long id, PostUpdateRequest request) {
        // 현재 사용자 닉네임 가져오기
        String currentUserNickname = securityUtil.getCurrentUserNickname();

        // 게시글 조회
        Post post = getPostById(id);

        // 작성자 확인 (작성자만 수정 가능)
        if (!post.getAuthor().equals(currentUserNickname)) {
            throw new AuthenticationException("게시글 수정 권한이 없습니다.");
        }

        // HTML에서 텍스트 추출 (최대 200자)
        String extractedContent = null;
        if (request.html() != null) {
            extractedContent = HtmlParser.extractText(request.html());
        }

        // 게시글 내용 업데이트
        post.update(
            request.title(),
            request.category(),
            extractedContent, // HTML에서 추출한 텍스트를 content로 사용
            request.html(),
            request.thumbnailUrl(),
            request.draft()
        );

        // 태그 처리 (기존 태그 삭제 후 새 태그 추가)
        tagService.clearTags(post);
        tagService.processTags(post, request.tags());

        // 파일 연결 처리
        // 기존 파일 연결은 유지하고, 새로운 파일만 연결
        if (request.html() != null) {
            mediaProcessorService.associateFilesFromHtml(post, request.html());
        }

        // 썸네일 처리
        if (request.thumbnailUrl() != null) {
            thumbnailService.processThumbnail(post, request.thumbnailUrl());
        }

        // 변경사항 저장
        Post updatedPost = postRepository.save(post);
        log.info("게시글 수정 완료: ID={}", id);

        // 수정된 게시글 상세 정보 반환
        List<PostFileMapping> fileMappings = postFileMappingRepository.findByPost(updatedPost);
        return PostDetailResponse.from(updatedPost, fileMappings);
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