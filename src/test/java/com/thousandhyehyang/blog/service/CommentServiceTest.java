package com.thousandhyehyang.blog.service;

import com.thousandhyehyang.blog.dto.comment.CommentCreateRequest;
import com.thousandhyehyang.blog.dto.comment.CommentResponse;
import com.thousandhyehyang.blog.dto.comment.CommentUpdateRequest;
import com.thousandhyehyang.blog.entity.Comment;
import com.thousandhyehyang.blog.entity.Post;
import com.thousandhyehyang.blog.exception.CommentNotFoundException;
import com.thousandhyehyang.blog.exception.PostNotFoundException;
import com.thousandhyehyang.blog.repository.CommentRepository;
import com.thousandhyehyang.blog.repository.PostRepository;
import com.thousandhyehyang.blog.service.comment.CommentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentService commentService;

    private Post testPost;
    private Comment testComment;
    private CommentCreateRequest createRequest;
    private CommentUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 게시글 설정
        testPost = mock(Post.class);
        when(testPost.getId()).thenReturn(1L);

        // 테스트용 댓글 설정
        testComment = mock(Comment.class);
        when(testComment.getId()).thenReturn(1L);
        when(testComment.getPost()).thenReturn(testPost);
        when(testComment.getNickname()).thenReturn("테스트닉네임");
        when(testComment.getContent()).thenReturn("테스트 댓글 내용");
        when(testComment.getEmoji()).thenReturn("😊");
        when(testComment.getBgColor()).thenReturn("#E0F7FA");
        when(testComment.getCreatedAt()).thenReturn(LocalDateTime.now());

        // 테스트용 요청 객체 설정
        createRequest = new CommentCreateRequest(
                "테스트닉네임",
                "테스트 댓글 내용",
                "😊",
                "#E0F7FA"
        );

        updateRequest = new CommentUpdateRequest(
                "수정된닉네임",
                "수정된 댓글 내용",
                "🎉",
                "#FFF9C4"
        );
    }

    @Test
    @DisplayName("댓글_생성_성공")
    void 댓글_생성_성공() {
        // given
        given(postRepository.findById(anyLong())).willReturn(Optional.of(testPost));
        given(commentRepository.save(any(Comment.class))).willReturn(testComment);

        // when
        CommentResponse response = commentService.createComment(1L, createRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testComment.getId());
        assertThat(response.getNickname()).isEqualTo(testComment.getNickname());
        verify(postRepository).findById(1L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글_생성_실패_존재하지_않는_게시글")
    void 댓글_생성_실패_존재하지_않는_게시글() {
        // given
        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(PostNotFoundException.class, () -> commentService.createComment(999L, createRequest));
        verify(postRepository).findById(999L);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("게시글별_댓글_목록_조회_성공")
    void 게시글별_댓글_목록_조회_성공() {
        // given
        given(postRepository.existsById(anyLong())).willReturn(true);
        given(commentRepository.findByPostIdOrderByCreatedAtDesc(anyLong())).willReturn(Arrays.asList(testComment));

        // when
        List<CommentResponse> comments = commentService.getCommentsByPostId(1L);

        // then
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getId()).isEqualTo(testComment.getId());
        assertThat(comments.get(0).getNickname()).isEqualTo(testComment.getNickname());
        verify(postRepository).existsById(1L);
        verify(commentRepository).findByPostIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("게시글별_댓글_목록_조회_실패_존재하지_않는_게시글")
    void 게시글별_댓글_목록_조회_실패_존재하지_않는_게시글() {
        // given
        given(postRepository.existsById(anyLong())).willReturn(false);

        // when & then
        assertThrows(PostNotFoundException.class, () -> commentService.getCommentsByPostId(999L));
        verify(postRepository).existsById(999L);
        verify(commentRepository, never()).findByPostIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    @DisplayName("댓글_수정_성공")
    void 댓글_수정_성공() {
        // given
        given(postRepository.existsById(anyLong())).willReturn(true);
        given(commentRepository.findById(anyLong())).willReturn(Optional.of(testComment));
        given(commentRepository.save(any(Comment.class))).willReturn(testComment);

        // 수정 후 값 설정
        when(testComment.getNickname()).thenReturn(updateRequest.getNickname());
        when(testComment.getContent()).thenReturn(updateRequest.getContent());
        when(testComment.getEmoji()).thenReturn(updateRequest.getEmoji());
        when(testComment.getBgColor()).thenReturn(updateRequest.getBgColor());

        // when
        CommentResponse response = commentService.updateComment(1L, 1L, updateRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getNickname()).isEqualTo(updateRequest.getNickname());
        assertThat(response.getContent()).isEqualTo(updateRequest.getContent());
        verify(postRepository).existsById(1L);
        verify(commentRepository).findById(1L);
        verify(testComment).update(
                updateRequest.getNickname(),
                updateRequest.getContent(),
                updateRequest.getEmoji(),
                updateRequest.getBgColor()
        );
        verify(commentRepository).save(testComment);
    }

    @Test
    @DisplayName("댓글_수정_실패_존재하지_않는_게시글")
    void 댓글_수정_실패_존재하지_않는_게시글() {
        // given
        given(postRepository.existsById(anyLong())).willReturn(false);

        // when & then
        assertThrows(PostNotFoundException.class, () -> commentService.updateComment(999L, 1L, updateRequest));
        verify(postRepository).existsById(999L);
        verify(commentRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("댓글_수정_실패_존재하지_않는_댓글")
    void 댓글_수정_실패_존재하지_않는_댓글() {
        // given
        given(postRepository.existsById(anyLong())).willReturn(true);
        given(commentRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(CommentNotFoundException.class, () -> commentService.updateComment(1L, 999L, updateRequest));
        verify(postRepository).existsById(1L);
        verify(commentRepository).findById(999L);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글_수정_실패_다른_게시글의_댓글")
    void 댓글_수정_실패_다른_게시글의_댓글() {
        // given
        Post otherPost = mock(Post.class);
        when(otherPost.getId()).thenReturn(2L);

        Comment commentFromOtherPost = mock(Comment.class);
        when(commentFromOtherPost.getPost()).thenReturn(otherPost);

        given(postRepository.existsById(anyLong())).willReturn(true);
        given(commentRepository.findById(anyLong())).willReturn(Optional.of(commentFromOtherPost));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> commentService.updateComment(1L, 1L, updateRequest));
        verify(postRepository).existsById(1L);
        verify(commentRepository).findById(1L);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글_삭제_성공")
    void 댓글_삭제_성공() {
        // given
        given(postRepository.existsById(anyLong())).willReturn(true);
        given(commentRepository.findById(anyLong())).willReturn(Optional.of(testComment));

        // when
        commentService.deleteComment(1L, 1L);

        // then
        verify(postRepository).existsById(1L);
        verify(commentRepository).findById(1L);
        verify(commentRepository).deleteById(1L);
    }

    @Test
    @DisplayName("댓글_삭제_실패_존재하지_않는_게시글")
    void 댓글_삭제_실패_존재하지_않는_게시글() {
        // given
        given(postRepository.existsById(anyLong())).willReturn(false);

        // when & then
        assertThrows(PostNotFoundException.class, () -> commentService.deleteComment(999L, 1L));
        verify(postRepository).existsById(999L);
        verify(commentRepository, never()).findById(anyLong());
        verify(commentRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("댓글_삭제_실패_존재하지_않는_댓글")
    void 댓글_삭제_실패_존재하지_않는_댓글() {
        // given
        given(postRepository.existsById(anyLong())).willReturn(true);
        given(commentRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(CommentNotFoundException.class, () -> commentService.deleteComment(1L, 999L));
        verify(postRepository).existsById(1L);
        verify(commentRepository).findById(999L);
        verify(commentRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("댓글_삭제_실패_다른_게시글의_댓글")
    void 댓글_삭제_실패_다른_게시글의_댓글() {
        // given
        Post otherPost = mock(Post.class);
        when(otherPost.getId()).thenReturn(2L);

        Comment commentFromOtherPost = mock(Comment.class);
        when(commentFromOtherPost.getPost()).thenReturn(otherPost);

        given(postRepository.existsById(anyLong())).willReturn(true);
        given(commentRepository.findById(anyLong())).willReturn(Optional.of(commentFromOtherPost));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> commentService.deleteComment(1L, 1L));
        verify(postRepository).existsById(1L);
        verify(commentRepository).findById(1L);
        verify(commentRepository, never()).deleteById(anyLong());
    }
}
