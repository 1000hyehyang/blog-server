package com.thousandhyehyang.blog.service;

import com.thousandhyehyang.blog.entity.Post;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 게시글 태그 관련 기능을 제공하는 서비스
 */
@Service
public class TagService {

    /**
     * 게시글 태그 처리
     * 요청에 포함된 태그 목록을 필터링하고 게시글에 추가합니다.
     *
     * @param post 태그를 추가할 게시글
     * @param tags 태그 목록
     */
    public void processTags(Post post, List<String> tags) {
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
    public boolean isValidTag(String tag) {
        return tag != null && !tag.isEmpty();
    }

    /**
     * 게시글의 모든 태그를 제거합니다.
     * 
     * @param post 태그를 제거할 게시글
     */
    public void clearTags(Post post) {
        post.clearTags();
    }
}