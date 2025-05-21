package com.thousandhyehyang.blog.service;

import com.thousandhyehyang.blog.entity.FileMetadata;
import com.thousandhyehyang.blog.entity.Post;
import com.thousandhyehyang.blog.entity.PostFileMapping;
import com.thousandhyehyang.blog.exception.PostNotFoundException;
import com.thousandhyehyang.blog.repository.FileMetadataRepository;
import com.thousandhyehyang.blog.repository.PostFileMappingRepository;
import com.thousandhyehyang.blog.repository.PostRepository;
import com.thousandhyehyang.blog.util.HtmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 게시글 미디어 처리 관련 기능을 제공하는 서비스
 */
@Service
public class MediaProcessorService {

    private static final Logger log = LoggerFactory.getLogger(MediaProcessorService.class);

    private final PostRepository postRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final PostFileMappingRepository postFileMappingRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MediaProcessorService(
            PostRepository postRepository,
            FileMetadataRepository fileMetadataRepository,
            PostFileMappingRepository postFileMappingRepository,
            ApplicationEventPublisher eventPublisher) {
        this.postRepository = postRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.postFileMappingRepository = postFileMappingRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * HTML 콘텐츠에서 파일을 추출하여 게시글과 연결
     * HTML 내용을 분석하여 포함된 미디어 파일(이미지, 비디오, 문서)을 찾아 게시글과 연결합니다.
     * 성능 개선을 위해 비동기적으로 처리합니다.
     *
     * @param post 파일과 연결할 게시글
     * @param html 파일을 추출할 HTML 콘텐츠
     */
    public void associateFilesFromHtml(Post post, String html) {
        if (html == null || html.isEmpty()) {
            return;
        }

        // HTML에서 미디어 URL 추출
        Map<String, List<String>> mediaUrls = HtmlParser.extractMediaUrls(html);

        // 비동기적으로 미디어 URL 처리를 위한 이벤트 발행
        eventPublisher.publishEvent(new MediaProcessingEvent(post.getId(), mediaUrls));

        log.info("게시글 ID={}의 미디어 파일 연결 작업이 비동기적으로 시작되었습니다.", post.getId());
    }

    /**
     * 미디어 URL을 처리하여 게시글과 연결
     * 추출된 미디어 URL을 유형별로 처리하여 게시글과 연결합니다.
     *
     * @param post 파일과 연결할 게시글
     * @param mediaUrls 처리할 미디어 URL 맵 (키: 미디어 유형, 값: URL 목록)
     */
    public void processMediaUrls(Post post, Map<String, List<String>> mediaUrls) {
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
    public void processMediaUrlsByType(Post post, List<String> urls, String mediaType) {
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
     * 파일을 게시글과 연결
     * 파일 URL을 기반으로 파일 메타데이터를 찾아 게시글과 연결합니다.
     * 이미 연결된 파일인 경우 중복 매핑을 무시하고 계속 진행합니다.
     *
     * @param post          파일과 연결할 게시글
     * @param fileUrl       파일의 URL
     * @param referenceType 참조 유형 (예: "THUMBNAIL", "IMAGE", "VIDEO", "DOCUMENT")
     */
    public void associateFileWithPost(Post post, String fileUrl, String referenceType) {
        // 파일 URL로 메타데이터 조회
        Optional<FileMetadata> fileMetadataOpt = fileMetadataRepository.findByPublicUrl(fileUrl);

        if (!fileMetadataOpt.isPresent()) {
            log.warn("파일을 찾을 수 없음: URL={}, 게시글 ID={}", fileUrl, post.getId());
            return;
        }

        FileMetadata fileMetadata = fileMetadataOpt.get();

        // 중복 매핑 확인 - 중복이면 건너뜀
        boolean isDuplicate = checkDuplicateFileMapping(post, fileMetadata, referenceType, fileUrl);
        if (!isDuplicate) {
            // 중복이 아닌 경우에만 게시글-파일 매핑 생성 및 저장
            createAndSaveFileMapping(post, fileMetadata, referenceType);
        }
    }

    /**
     * 중복 파일 매핑 확인
     * 이미 동일한 파일이 동일한 참조 유형으로 연결되어 있는지 확인합니다.
     * 중복 매핑이 발견된 경우 로그만 남기고 계속 진행합니다.
     *
     * @param post 게시글
     * @param fileMetadata 파일 메타데이터
     * @param referenceType 참조 유형
     * @param fileUrl 파일 URL (로깅용)
     * @return 중복 매핑이 발견되었는지 여부 (true: 중복 발견, false: 중복 없음)
     */
    public boolean checkDuplicateFileMapping(Post post, FileMetadata fileMetadata, String referenceType, String fileUrl) {
        if (postFileMappingRepository.existsByPostAndFileAndReferenceType(post, fileMetadata, referenceType)) {
            log.debug("중복 파일 매핑 감지: 게시글_ID={}, 파일_URL={}, 참조_유형={} - 무시하고 계속 진행", 
                    post.getId(), fileUrl, referenceType);
            return true;
        }
        return false;
    }

    /**
     * 파일 매핑 생성 및 저장
     * 게시글과 파일 간의 매핑을 생성하고 저장합니다.
     *
     * @param post 게시글
     * @param fileMetadata 파일 메타데이터
     * @param referenceType 참조 유형
     */
    public void createAndSaveFileMapping(Post post, FileMetadata fileMetadata, String referenceType) {
        PostFileMapping mapping = new PostFileMapping(post, fileMetadata, referenceType);
        postFileMappingRepository.save(mapping);
        log.debug("파일이 게시글과 연결됨: 게시글_ID={}, 파일_ID={}, 참조_유형={}", 
                post.getId(), fileMetadata.getId(), referenceType);
    }

    /**
     * 미디어 처리 이벤트 리스너
     * 비동기적으로 미디어 URL을 처리하여 게시글과 연결합니다.
     *
     * @param event 미디어 처리 이벤트
     */
    @EventListener
    @Async("taskExecutor")
    @Transactional
    public void handleMediaProcessingEvent(MediaProcessingEvent event) {
        try {
            log.debug("비동기 미디어 처리 시작: 게시글_ID={}", event.getPostId());

            // 게시글 조회
            Post post = postRepository.findById(event.getPostId())
                    .orElseThrow(() -> new PostNotFoundException(event.getPostId()));

            // 미디어 URL 처리
            processMediaUrls(post, event.getMediaUrls());

            log.info("비동기 미디어 처리 완료: 게시글_ID={}", event.getPostId());
        } catch (Exception e) {
            log.error("비동기 미디어 처리 중 오류 발생: 게시글_ID={}", event.getPostId(), e);
        }
    }

    /**
     * 미디어 처리 이벤트 클래스
     * 비동기적으로 미디어 URL을 처리하기 위한 이벤트 클래스입니다.
     */
    public static class MediaProcessingEvent {
        private final Long postId;
        private final Map<String, List<String>> mediaUrls;

        public MediaProcessingEvent(Long postId, Map<String, List<String>> mediaUrls) {
            this.postId = postId;
            this.mediaUrls = mediaUrls;
        }

        public Long getPostId() {
            return postId;
        }

        public Map<String, List<String>> getMediaUrls() {
            return mediaUrls;
        }
    }
}