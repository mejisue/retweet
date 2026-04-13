package mejisue.backend.domain.comment.service;

import mejisue.backend.common.exception.BusinessException;
import mejisue.backend.common.exception.ErrorCode;
import mejisue.backend.domain.comment.dto.CommentCreateRequest;
import mejisue.backend.domain.comment.dto.CommentResponse;
import mejisue.backend.domain.comment.dto.CommentUpdateRequest;
import mejisue.backend.domain.comment.entity.Comment;
import mejisue.backend.domain.comment.repository.CommentRepository;
import mejisue.backend.domain.member.entity.Member;
import mejisue.backend.domain.post.entity.Post;
import mejisue.backend.domain.post.repository.PostRepository;
import mejisue.backend.domain.profile.entity.Profile;
import mejisue.backend.domain.profile.repository.ProfileRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommentServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock PostRepository postRepository;
    @Mock ProfileRepository profileRepository;

    @InjectMocks CommentService commentService;

    private Member member;
    private Post post;
    private Comment comment;
    private Profile profile;

    @BeforeEach
    void setUp() {
        member = Member.ofLocal("test@example.com", "encoded");
        setField(member, "id", 1L);

        post = mock(Post.class);
        given(post.getId()).willReturn(1L);

        comment = mock(Comment.class);
        given(comment.getId()).willReturn(1L);
        given(comment.getPost()).willReturn(post);
        given(comment.getMember()).willReturn(member);
        given(comment.getContent()).willReturn("댓글 내용");
        given(comment.getParentComment()).willReturn(null);
        given(comment.getRootComment()).willReturn(null);
        given(comment.getCreatedAt()).willReturn(LocalDateTime.now());
        given(comment.getUpdatedAt()).willReturn(LocalDateTime.now());

        profile = mock(Profile.class);
        given(profile.getNickname()).willReturn("닉네임");
        given(profile.getAvatarUrl()).willReturn(null);
    }

    // ────────────────────────────────────────────
    // 댓글 작성
    // ────────────────────────────────────────────

    @Test
    @DisplayName("최상위 댓글 작성 성공")
    void create_rootComment_success() {
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(commentRepository.save(any())).willReturn(comment);
        given(profileRepository.findByMemberId(1L)).willReturn(Optional.of(profile));

        CommentResponse result = commentService.create(1L, member, new CommentCreateRequest("댓글 내용", null, null));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.postId()).isEqualTo(1L);
        assertThat(result.content()).isEqualTo("댓글 내용");
        assertThat(result.rootCommentId()).isNull();
        assertThat(result.parentCommentId()).isNull();
        then(commentRepository).should().save(any());
    }

    @Test
    @DisplayName("대댓글 작성 성공")
    void create_reply_success() {
        Comment rootComment = mock(Comment.class);
        given(rootComment.getId()).willReturn(2L);

        Comment replyComment = mock(Comment.class);
        given(replyComment.getId()).willReturn(3L);
        given(replyComment.getPost()).willReturn(post);
        given(replyComment.getMember()).willReturn(member);
        given(replyComment.getContent()).willReturn("대댓글 내용");
        given(replyComment.getRootComment()).willReturn(rootComment);
        given(replyComment.getParentComment()).willReturn(rootComment);
        given(replyComment.getCreatedAt()).willReturn(LocalDateTime.now());
        given(replyComment.getUpdatedAt()).willReturn(LocalDateTime.now());

        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(commentRepository.findById(2L)).willReturn(Optional.of(rootComment));
        given(commentRepository.save(any())).willReturn(replyComment);
        given(profileRepository.findByMemberId(1L)).willReturn(Optional.of(profile));

        CommentResponse result = commentService.create(1L, member, new CommentCreateRequest("대댓글 내용", 2L, 2L));

        assertThat(result.rootCommentId()).isEqualTo(2L);
        assertThat(result.parentCommentId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 댓글 → POST_NOT_FOUND 예외")
    void create_postNotFound_throwsException() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(999L, member, new CommentCreateRequest("내용", null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 부모 댓글에 대댓글 → COMMENT_NOT_FOUND 예외")
    void create_parentCommentNotFound_throwsException() {
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(commentRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(1L, member, new CommentCreateRequest("내용", 999L, 999L)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
    }

    // ────────────────────────────────────────────
    // 댓글 수정
    // ────────────────────────────────────────────

    @Test
    @DisplayName("댓글 수정 성공")
    void update_success() {
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
        given(profileRepository.findByMemberId(1L)).willReturn(Optional.of(profile));

        CommentResponse result = commentService.update(1L, member, new CommentUpdateRequest("수정된 내용"));

        then(comment).should().update("수정된 내용");
        assertThat(result.author().nickname()).isEqualTo("닉네임");
    }

    @Test
    @DisplayName("다른 사람의 댓글 수정 → FORBIDDEN 예외")
    void update_forbidden() {
        Member other = Member.ofLocal("other@example.com", "encoded");
        setField(other, "id", 2L);

        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.update(1L, other, new CommentUpdateRequest("수정")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        then(comment).should(never()).update(any());
    }

    @Test
    @DisplayName("존재하지 않는 댓글 수정 → COMMENT_NOT_FOUND 예외")
    void update_commentNotFound() {
        given(commentRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.update(999L, member, new CommentUpdateRequest("수정")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
    }

    // ────────────────────────────────────────────
    // 댓글 삭제
    // ────────────────────────────────────────────

    @Test
    @DisplayName("댓글 삭제 성공")
    void delete_success() {
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        commentService.delete(1L, member);

        then(commentRepository).should().delete(comment);
    }

    @Test
    @DisplayName("다른 사람의 댓글 삭제 → FORBIDDEN 예외")
    void delete_forbidden() {
        Member other = Member.ofLocal("other@example.com", "encoded");
        setField(other, "id", 2L);

        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.delete(1L, other))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        then(commentRepository).should(never()).delete(any(Comment.class));
    }

    // ────────────────────────────────────────────
    // 댓글 목록 조회
    // ────────────────────────────────────────────

    @Test
    @DisplayName("게시글 댓글 목록 조회 성공")
    void findAllByPostId_returnsList() {
        given(commentRepository.findAllByPostIdWithMember(1L)).willReturn(List.of(comment));
        given(profileRepository.findByMemberId(1L)).willReturn(Optional.of(profile));

        List<CommentResponse> result = commentService.findAllByPostId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).content()).isEqualTo("댓글 내용");
        assertThat(result.get(0).author().nickname()).isEqualTo("닉네임");
    }

    @Test
    @DisplayName("댓글 없는 게시글 조회 → 빈 목록 반환")
    void findAllByPostId_emptyList() {
        given(commentRepository.findAllByPostIdWithMember(1L)).willReturn(List.of());

        List<CommentResponse> result = commentService.findAllByPostId(1L);

        assertThat(result).isEmpty();
    }

    // ────────────────────────────────────────────
    // Helper
    // ────────────────────────────────────────────

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
