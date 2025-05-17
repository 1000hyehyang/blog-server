package com.thousandhyehyang.blog.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML 콘텐츠에서 미디어 URL을 추출하기 위한 유틸리티 클래스
 */
public class HtmlParser {

    // img 태그와 일치하고 src 속성을 추출하는 패턴
    private static final Pattern IMG_PATTERN = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");

    // video 태그와 일치하고 src 속성을 추출하는 패턴
    private static final Pattern VIDEO_PATTERN = Pattern.compile("<video[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");

    // video 태그 내부의 source 태그와 일치하고 src 속성을 추출하는 패턴
    private static final Pattern SOURCE_PATTERN = Pattern.compile("<source[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");

    // a 태그와 일치하고 href 속성을 추출하는 패턴
    private static final Pattern ANCHOR_PATTERN = Pattern.compile("<a[^>]+href\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");

    /**
     * HTML 콘텐츠에서 모든 미디어 URL 추출
     * 
     * @param html 파싱할 HTML 콘텐츠
     * @return 미디어 타입별 URL 목록을 담은 맵
     */
    public static Map<String, List<String>> extractMediaUrls(String html) {
        Map<String, List<String>> mediaUrls = new HashMap<>();
        mediaUrls.put("IMAGE", extractUrls(html, IMG_PATTERN));
        mediaUrls.put("VIDEO", extractVideoUrls(html));
        mediaUrls.put("DOCUMENT", extractDocumentUrls(html));

        return mediaUrls;
    }

    /**
     * 패턴을 사용하여 URL 추출
     * 
     * @param html 파싱할 HTML 콘텐츠
     * @param pattern 추출에 사용할 패턴
     * @return 추출된 URL 목록
     */
    private static List<String> extractUrls(String html, Pattern pattern) {
        List<String> urls = new ArrayList<>();
        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            String url = matcher.group(1);
            if (url != null && !url.isEmpty()) {
                urls.add(url);
            }
        }

        return urls;
    }

    /**
     * HTML 콘텐츠에서 비디오 URL 추출
     * 
     * @param html 파싱할 HTML 콘텐츠
     * @return 비디오 URL 목록
     */
    private static List<String> extractVideoUrls(String html) {
        List<String> urls = new ArrayList<>();

        // video 태그의 src 속성 추출
        urls.addAll(extractUrls(html, VIDEO_PATTERN));

        // source 태그의 src 속성 추출
        urls.addAll(extractUrls(html, SOURCE_PATTERN));

        return urls;
    }

    /**
     * HTML 콘텐츠에서 문서 URL 추출
     * 
     * @param html 파싱할 HTML 콘텐츠
     * @return 문서 URL 목록
     */
    private static List<String> extractDocumentUrls(String html) {
        List<String> urls = new ArrayList<>();
        Matcher matcher = ANCHOR_PATTERN.matcher(html);

        while (matcher.find()) {
            String url = matcher.group(1);
            if (url != null && !url.isEmpty() && isDocumentUrl(url)) {
                urls.add(url);
            }
        }

        return urls;
    }

    /**
     * URL이 문서일 가능성이 있는지 확인
     * 
     * @param url 확인할 URL
     * @return URL이 문서일 가능성이 있으면 true
     */
    private static boolean isDocumentUrl(String url) {
        String lowerUrl = url.toLowerCase();
        return lowerUrl.contains("/documents/") || 
               lowerUrl.endsWith(".pdf") || 
               lowerUrl.endsWith(".doc") || 
               lowerUrl.endsWith(".docx") || 
               lowerUrl.endsWith(".xls") || 
               lowerUrl.endsWith(".xlsx") || 
               lowerUrl.endsWith(".ppt") || 
               lowerUrl.endsWith(".pptx") || 
               lowerUrl.endsWith(".txt");
    }
}
