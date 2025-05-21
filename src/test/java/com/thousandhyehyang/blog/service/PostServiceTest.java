package com.thousandhyehyang.blog.service;

import com.thousandhyehyang.blog.dto.PostCreateRequest;
import com.thousandhyehyang.blog.dto.PostDetailResponse;
import com.thousandhyehyang.blog.dto.PostSummaryResponse;
import com.thousandhyehyang.blog.dto.PostUpdateRequest;
import com.thousandhyehyang.blog.entity.Account;
import com.thousandhyehyang.blog.entity.FileMetadata;
import com.thousandhyehyang.blog.entity.Post;
import com.thousandhyehyang.blog.entity.PostFileMapping;
import com.thousandhyehyang.blog.exception.AuthenticationException;
import com.thousandhyehyang.blog.exception.PostNotFoundException;
import com.thousandhyehyang.blog.repository.FileMetadataRepository;
import com.thousandhyehyang.blog.repository.PostFileMappingRepository;
import com.thousandhyehyang.blog.repository.PostRepository;
import com.thousandhyehyang.blog.util.HtmlParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private PostFileMappingRepository postFileMappingRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PostService postService;

    private Account testAccount;
    private Post testPost;
    private PostCreateRequest createRequest;
    private PostUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 계정 설정
        testAccount = mock(Account.class);
        when(testAccount.getNickname()).thenReturn("테스트사용자");

        // SecurityContext 모킹
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testAccount);

        // 테스트용 게시글 설정
        testPost = new Post(
                "테스트 제목",
                "테스트",
                "테스트 내용",
                "<p>테스트 HTML 내용</p>",
                "https://example.com/thumbnail.jpg",
                "테스트사용자",
                false
        );
        // ID는 Mockito에서 when().thenReturn() 패턴으로 처리
        when(testPost.getId()).thenReturn(1L);
        when(testPost.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(testPost.getUpdatedAt()).thenReturn(LocalDateTime.now());

        // 테스트용 요청 객체 설정
        createRequest = new PostCreateRequest(
                "테스트 제목",
                "테스트",
                "테스트 내용",
                "<p>테스트 HTML 내용</p>",
                "https://example.com/thumbnail.jpg",
                List.of("태그1", "태그2"),
                false
        );

        updateRequest = new PostUpdateRequest(
                "수정된 제목",
                "수정된카테고리",
                "수정된 내용",
                "<p>수정된 HTML 내용</p>",
                "https://example.com/updated-thumbnail.jpg",
                List.of("태그1", "태그3"),
                false
        );
    }

    @Test
    @DisplayName("게시글_생성_성공")
    void 게시글_생성_성공() {
        // given
        given(postRepository.save(any(Post.class))).willReturn(testPost);

        // HTML 파서 모킹
        try (MockedStatic<HtmlParser> htmlParserMock = mockStatic(HtmlParser.class)) {
            htmlParserMock.when(() -> HtmlParser.extractText(anyString())).thenReturn("테스트 내용");
            htmlParserMock.when(() -> HtmlParser.extractMediaUrls(anyString())).thenReturn(Collections.emptyMap());

            // when
            Long postId = postService.create(createRequest);

            // then
            assertThat(postId).isEqualTo(testPost.getId());
            verify(postRepository).save(any(Post.class));
        }
    }

    @Test
    @DisplayName("게시글_생성_실패_인증정보_없음")
    void 게시글_생성_실패_인증정보_없음() {
        // given
        when(securityContext.getAuthentication()).thenReturn(null);

        // when & then
        assertThrows(AuthenticationException.class, () -> postService.create(createRequest));
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글_조회_성공")
    void 게시글_조회_성공() {
        // given
        given(postRepository.findById(anyLong())).willReturn(Optional.of(testPost));
        given(postRepository.findByIdWithTags(anyLong())).willReturn(Optional.of(testPost));
        given(postFileMappingRepository.findByPost(any(Post.class))).willReturn(Collections.emptyList());

        // when
        PostDetailResponse response = postService.getPostDetail(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(testPost.getId());
        assertThat(response.title()).isEqualTo(testPost.getTitle());
        verify(postRepository).findByIdWithTags(1L);
    }

    @Test
    @DisplayName("게시글_조회_실패_존재하지_않는_게시글")
    void 게시글_조회_실패_존재하지_않는_게시글() {
        // given
        given(postRepository.findByIdWithTags(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(PostNotFoundException.class, () -> postService.getPostDetail(999L));
    }

    @Test
    @DisplayName("임시저장_게시글_조회_성공")
    void 임시저장_게시글_조회_성공() {
        // given
        Post draftPost = new Post(
                "임시저장 제목",
                "테스트",
                "임시저장 내용",
                "<p>임시저장 HTML 내용</p>",
                null,
                "테스트사용자",
                true
        );
        when(draftPost.getId()).thenReturn(2L);
        when(draftPost.isDraft()).thenReturn(true);

        given(postRepository.findByAuthorAndDraftTrueOrderByCreatedAtDesc(anyString()))
                .willReturn(List.of(draftPost));

        // when
        List<PostSummaryResponse> draftPosts = postService.getDraftPosts();

        // then
        assertThat(draftPosts).hasSize(1);
        assertThat(draftPosts.get(0).id()).isEqualTo(draftPost.getId());
        assertThat(draftPosts.get(0).title()).isEqualTo(draftPost.getTitle());
        // PostSummaryResponse에는 isDraft 메서드가 없으므로 해당 검증은 제거
    }

    @Test
    @DisplayName("임시저장_게시글_조회_실패_인증정보_없음")
    void 임시저장_게시글_조회_실패_인증정보_없음() {
        // given
        when(securityContext.getAuthentication()).thenReturn(null);

        // when & then
        assertThrows(AuthenticationException.class, () -> postService.getDraftPosts());
    }

    @Test
    @DisplayName("최근_게시글_목록_조회_성공")
    void 최근_게시글_목록_조회_성공() {
        // given
        Post post2 = new Post("제목2", "카테고리2", "내용2", "<p>HTML2</p>", null, "작성자2", false);
        when(post2.getId()).thenReturn(2L);

        List<Post> recentPosts = Arrays.asList(testPost, post2);

        given(postRepository.findRecentPosts(any(Pageable.class))).willReturn(recentPosts);

        // when
        List<PostSummaryResponse> result = postService.getRecentPosts(5);

        // then
        assertThat(result).hasSize(2);
        verify(postRepository).findRecentPosts(PageRequest.of(0, 5));
    }

    @Test
    @DisplayName("게시글_수정_성공")
    void 게시글_수정_성공() {
        // given
        given(postRepository.findById(anyLong())).willReturn(Optional.of(testPost));
        given(postRepository.save(any(Post.class))).willReturn(testPost);
        given(postFileMappingRepository.findByPost(any(Post.class))).willReturn(Collections.emptyList());

        // HTML 파서 모킹
        try (MockedStatic<HtmlParser> htmlParserMock = mockStatic(HtmlParser.class)) {
            htmlParserMock.when(() -> HtmlParser.extractText(anyString())).thenReturn("수정된 내용");
            htmlParserMock.when(() -> HtmlParser.extractMediaUrls(anyString())).thenReturn(Collections.emptyMap());

            // when
            PostDetailResponse response = postService.updatePost(1L, updateRequest);

            // then
            assertThat(response).isNotNull();
            verify(postRepository).save(any(Post.class));
        }
    }

    @Test
    @DisplayName("게시글_수정_실패_권한없음")
    void 게시글_수정_실패_권한없음() {
        // given
        Post otherUserPost = new Post(
                "다른 사용자 게시글",
                "테스트",
                "다른 사용자 내용",
                "<p>다른 사용자 HTML</p>",
                null,
                "다른사용자",
                false
        );
        when(otherUserPost.getId()).thenReturn(3L);

        given(postRepository.findById(anyLong())).willReturn(Optional.of(otherUserPost));

        // when & then
        assertThrows(AuthenticationException.class, () -> postService.updatePost(3L, updateRequest));
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글_삭제_성공")
    void 게시글_삭제_성공() {
        // given
        given(postRepository.findById(anyLong())).willReturn(Optional.of(testPost));
        given(postFileMappingRepository.findByPost(any(Post.class))).willReturn(Collections.emptyList());

        // when
        postService.deletePost(1L);

        // then
        verify(postRepository).delete(testPost);
    }

    @Test
    @DisplayName("게시글_삭제_실패_존재하지_않는_게시글")
    void 게시글_삭제_실패_존재하지_않는_게시글() {
        // given
        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(PostNotFoundException.class, () -> postService.deletePost(999L));
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("임시저장_게시글_조회_실패_다른사용자_접근")
    void 임시저장_게시글_조회_실패_다른사용자_접근() {
        // given
        Post draftPost = new Post(
                "임시저장 제목",
                "테스트",
                "임시저장 내용",
                "<p>임시저장 HTML 내용</p>",
                null,
                "다른사용자",
                true
        );
        when(draftPost.getId()).thenReturn(4L);

        given(postRepository.findByIdWithTags(anyLong())).willReturn(Optional.of(draftPost));

        // when & then
        assertThrows(AuthenticationException.class, () -> postService.getPostDetail(4L));
    }

    @Test
    @DisplayName("게시글_파일_연결_성공")
    void 게시글_파일_연결_성공() {
        // given
        FileMetadata fileMetadata = mock(FileMetadata.class);
        when(fileMetadata.getId()).thenReturn(1L);
        when(fileMetadata.getPublicUrl()).thenReturn("https://example.com/thumbnail.jpg");

        given(postRepository.save(any(Post.class))).willReturn(testPost);
        given(fileMetadataRepository.findByPublicUrl(anyString())).willReturn(Optional.of(fileMetadata));
        given(postFileMappingRepository.existsByPostAndFileAndReferenceType(any(), any(), anyString())).willReturn(false);

        // HTML 파서 모킹
        try (MockedStatic<HtmlParser> htmlParserMock = mockStatic(HtmlParser.class)) {
            htmlParserMock.when(() -> HtmlParser.extractText(anyString())).thenReturn("테스트 내용");
            Map<String, List<String>> mediaUrls = new HashMap<>();
            mediaUrls.put("IMAGE", List.of("https://example.com/image.jpg"));
            htmlParserMock.when(() -> HtmlParser.extractMediaUrls(anyString())).thenReturn(mediaUrls);

            // when
            Long postId = postService.create(createRequest);

            // then
            assertThat(postId).isEqualTo(testPost.getId());
            verify(postFileMappingRepository, atLeastOnce()).save(any(PostFileMapping.class));
        }
    }
}
