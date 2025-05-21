package com.thousandhyehyang.blog.service;

import com.thousandhyehyang.blog.entity.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 게시글 썸네일 관련 기능을 제공하는 서비스
 */
@Service
public class ThumbnailService {

    private static final Logger log = LoggerFactory.getLogger(ThumbnailService.class);
    private final MediaProcessorService mediaProcessorService;

    public ThumbnailService(MediaProcessorService mediaProcessorService) {
        this.mediaProcessorService = mediaProcessorService;
    }

    /**
     * 썸네일 처리
     * 썸네일 URL이 있는 경우 게시글과 연결합니다.
     *
     * @param post 썸네일을 연결할 게시글
     * @param thumbnailUrl 썸네일 URL
     */
    public void processThumbnail(Post post, String thumbnailUrl) {
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            mediaProcessorService.associateFileWithPost(post, thumbnailUrl, "THUMBNAIL");
            
            // Ensure the thumbnailUrl is set on the Post entity
            if (post.getThumbnailUrl() == null || !post.getThumbnailUrl().equals(thumbnailUrl)) {
                post.update(null, null, null, null, thumbnailUrl);
                log.debug("썸네일 URL이 게시글에 저장됨: 게시글_ID={}, 썸네일_URL={}", post.getId(), thumbnailUrl);
            }
        }
    }
}