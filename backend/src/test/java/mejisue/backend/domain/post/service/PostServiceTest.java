package mejisue.backend.domain.post.service;

import mejisue.backend.common.exception.BusinessException;
import mejisue.backend.common.exception.ErrorCode;
import mejisue.backend.domain.comment.repository.CommentRepository;
import mejisue.backend.domain.like.entity.PostLike;
import mejisue.backend.domain.like.repository.PostLikeRepository;
import mejisue.backend.domain.member.entity.Member;
import mejisue.backend.domain.post.dto.PostCreateRequest;
import mejisue.backend.domain.post.dto.PostResponse;
import mejisue.backend.domain.post.dto.PostUpdateRequest;
import mejisue.backend.domain.post.entity.Post;
import mejisue.backend.domain.post.repository.PostRepository;
import mejisue.backend.domain.profile.entity.Profile;
import mejisue.backend.domain.profile.repository.ProfileRepository;
import mejisue.backend.infra.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostServiceTest {

    @Mock PostRepository postRepository;
    @Mock ProfileRepository profileRepository;
    @Mock PostLikeRepository postLikeRepository;
    @Mock CommentRepository commentRepository;
    @Mock S3Service s3Service;

    @InjectMocks PostService postService;

    private Member member;
    private Post post;
    private Profile profile;

    @BeforeEach
    void setUp() {
        member = Member.ofLocal("test@example.com", "encoded");
        setField(member, "id", 1L);

        post = mock(Post.class);
        given(post.getId()).willReturn(1L);
        given(post.getContent()).willReturn("테스트 내용");
        given(post.getImageUrls()).willReturn(List.of());
        given(post.getLikeCount()).willReturn(0);
        given(post.getMember()).willReturn(member);
        given(post.getCreatedAt()).willReturn(LocalDateTime.now());
        given(post.getUpdatedAt()).willReturn(LocalDateTime.now());

        profile = mock(Profile.class);
        given(profile.getNickname()).willReturn("닉네임");
        given(profile.getAvatarUrl()).willReturn(null);
    }

    // ────────────────────────────────────────────
    // 게시글 작성
    // ────────────────────────────────────────────

    @Test
    @DisplayName("게시글 작성 성공")
    void create_success() {
        PostCreateRequest request = new PostCreateRequest("테스트 내용", List.of());

        given(postRepository.save(any())).willReturn(post);
        given(profileRepository.findByMemberId(1L)).willReturn(Optional.of(profile));

        PostResponse result = postService.create(member, request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.content()).isEqualTo("테스트 내용");
        assertThat(result.author().nickname()).isEqualTo("닉네임");
        then(postRepository).should().save(any());
    }

    // ────────────────────────────────────────────
    // 게시글 단건 조회
    // ────────────────────────────────────────────

    @Test
    @DisplayName("게시글 단건 조회 성공 - 좋아요 안 한 경우 isLiked=false")
    void findById_success_notLiked() {
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(profileRepository.findByMemberId(1L)).willReturn(Optional.of(profile));
        given(postLikeRepository.existsByMemberIdAndPostId(1L, 1L)).willReturn(false);

        PostResponse result = postService.findById(1L, 1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.content()).isEqualTo("테스트 내용");
        assertThat(result.isLiked()).isFalse();
    }

    @Test
    @DisplayName("게시글 단건 조회 성공 - 좋아요 한 경우 isLiked=true")
    void findById_success_liked() {
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(profileRepository.findByMemberId(1L)).willReturn(Optional.of(profile));
        given(postLikeRepository.existsByMemberIdAndPostId(1L, 1L)).willReturn(true);

        PostResponse result = postService.findById(1L, 1L);

        assertThat(result.isLiked()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 → POST_NOT_FOUND 예외")
    void findById_notFound() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.findById(999L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    // ────────────────────────────────────────────
    // 게시글 목록 조회
    // ────────────────────────────────────────────

    @Test
    @DisplayName("전체 게시글 목록 조회 성공")
    void findAll_returnsPosts() {
        Slice<Post> postSlice = new SliceImpl<>(List.of(post), PageRequest.of(0, 10), false);

        given(postRepository.findAllByOrderByCreatedAtDesc(any())).willReturn(postSlice);
        given(profileRepository.findByMemberId(1L)).willReturn(Optional.of(profile));
        given(postLikeRepository.existsByMemberIdAndPostId(1L, 1L)).willReturn(false);

        Slice<PostResponse> result = postService.findAll(1L, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).content()).isEqualTo("테스트 내용");
    }

    @Test
    @DisplayName("특정 회원 게시글 목록 조회 성공")
    void findByMemberId_returnsPosts() {
        Slice<Post> postSlice = new SliceImpl<>(List.of(post), PageRequest.of(0, 10), false);

        given(postRepository.findAllByMemberIdOrderByCreatedAtDesc(1L, PageRequest.of(0, 10))).willReturn(postSlice);
        given(profileRepository.findByMemberId(1L)).willReturn(Optional.of(profile));
        given(postLikeRepository.existsByMemberIdAndPostId(1L, 1L)).willReturn(false);

        Slice<PostResponse> result = postService.findByMemberId(1L, 1L, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    // ────────────────────────────────────────────
    // 게시글 수정
    // ────────────────────────────────────────────

    @Test
    @DisplayName("게시글 수정 성공")
    void update_success() {
        PostUpdateRequest request = new PostUpdateRequest("수정된 내용", List.of());

        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(profileRepository.findByMemberId(1L)).willReturn(Optional.of(profile));
        given(postLikeRepository.existsByMemberIdAndPostId(1L, 1L)).willReturn(false);

        PostResponse result = postService.update(1L, member, request);

        then(post).should().update("수정된 내용", List.of());
        assertThat(result.author().nickname()).isEqualTo("닉네임");
    }

    @Test
    @DisplayName("다른 사람의 게시글 수정 → FORBIDDEN 예외")
    void update_forbidden() {
        Member other = Member.ofLocal("other@example.com", "encoded");
        setField(other, "id", 2L);

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.update(1L, other, new PostUpdateRequest("수정", List.of())))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        then(post).should(never()).update(any(), any());
    }

    // ────────────────────────────────────────────
    // 게시글 삭제
    // ────────────────────────────────────────────

    @Test
    @DisplayName("이미지 없는 게시글 삭제 → S3 호출 없이 댓글·좋아요·게시글 순서대로 삭제")
    void delete_noImages_success() {
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(post.getImageUrls()).willReturn(List.of());

        postService.delete(1L, member);

        then(s3Service).shouldHaveNoInteractions();
        InOrder order = inOrder(commentRepository, postLikeRepository, postRepository);
        order.verify(commentRepository).clearSelfReferencesByPostId(1L);
        order.verify(commentRepository).deleteAllByPostId(1L);
        order.verify(postLikeRepository).deleteAllByPostId(1L);
        order.verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("이미지 있는 게시글 삭제 → S3 삭제 후 댓글·좋아요·게시글 순서대로 삭제")
    void delete_withImages_deletesS3First() {
        List<String> imageUrls = List.of(
                "https://cdn.cloudfront.net/posts/1/a.jpg",
                "https://cdn.cloudfront.net/posts/1/b.png"
        );
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(post.getImageUrls()).willReturn(imageUrls);

        postService.delete(1L, member);

        InOrder order = inOrder(s3Service, commentRepository, postLikeRepository, postRepository);
        order.verify(s3Service).deleteAll(imageUrls);
        order.verify(commentRepository).clearSelfReferencesByPostId(1L);
        order.verify(commentRepository).deleteAllByPostId(1L);
        order.verify(postLikeRepository).deleteAllByPostId(1L);
        order.verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("댓글 삭제 시 자기 참조 FK 제거 후 전체 삭제 순서 보장")
    void delete_commentsDeletedInCorrectOrder() {
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(post.getImageUrls()).willReturn(List.of());

        postService.delete(1L, member);

        InOrder order = inOrder(commentRepository);
        order.verify(commentRepository).clearSelfReferencesByPostId(1L);
        order.verify(commentRepository).deleteAllByPostId(1L);
    }

    @Test
    @DisplayName("다른 사람의 게시글 삭제 → FORBIDDEN 예외, 댓글·좋아요·게시글 삭제 미호출")
    void delete_forbidden() {
        Member other = Member.ofLocal("other@example.com", "encoded");
        setField(other, "id", 2L);

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.delete(1L, other))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);

        then(commentRepository).shouldHaveNoInteractions();
        then(postLikeRepository).shouldHaveNoInteractions();
        then(postRepository).should(never()).delete(any(Post.class));
    }

    // ────────────────────────────────────────────
    // 좋아요 토글
    // ────────────────────────────────────────────

    @Test
    @DisplayName("좋아요 추가 → true 반환")
    void toggleLike_like() {
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.existsByMemberIdAndPostId(1L, 1L)).willReturn(false);

        boolean result = postService.toggleLike(1L, member);

        assertThat(result).isTrue();
        then(postLikeRepository).should().save(any(PostLike.class));
        then(post).should().increaseLikeCount();
    }

    @Test
    @DisplayName("좋아요 취소 → false 반환")
    void toggleLike_unlike() {
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.existsByMemberIdAndPostId(1L, 1L)).willReturn(true);

        boolean result = postService.toggleLike(1L, member);

        assertThat(result).isFalse();
        then(postLikeRepository).should().deleteByMemberIdAndPostId(1L, 1L);
        then(post).should().decreaseLikeCount();
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
