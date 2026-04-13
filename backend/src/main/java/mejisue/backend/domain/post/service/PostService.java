package mejisue.backend.domain.post.service;

import lombok.RequiredArgsConstructor;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final S3Service s3Service;

    public PostResponse create(Member member, PostCreateRequest request) {
        Post post = postRepository.save(Post.create(member, request.content(), request.imageUrls()));
        Profile profile = getProfile(member.getId());
        return PostResponse.of(post, profile, false);
    }

    @Transactional(readOnly = true)
    public PostResponse findById(Long postId, Long memberId) {
        Post post = getPost(postId);
        Profile profile = getProfile(post.getMember().getId());
        boolean isLiked = postLikeRepository.existsByMemberIdAndPostId(memberId, postId);
        return PostResponse.of(post, profile, isLiked);
    }

    @Transactional(readOnly = true)
    public Slice<PostResponse> findAll(Long memberId, Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(post -> PostResponse.of(post, getProfile(post.getMember().getId()),
                        postLikeRepository.existsByMemberIdAndPostId(memberId, post.getId())));
    }

    @Transactional(readOnly = true)
    public Slice<PostResponse> findByMemberId(Long memberId, Long requesterId, Pageable pageable) {
        Profile profile = getProfile(memberId);
        return postRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(post -> PostResponse.of(post, profile,
                        postLikeRepository.existsByMemberIdAndPostId(requesterId, post.getId())));
    }

    public PostResponse update(Long postId, Member member, PostUpdateRequest request) {
        Post post = getOwnedPost(postId, member.getId());
        post.update(request.content(), request.imageUrls());
        boolean isLiked = postLikeRepository.existsByMemberIdAndPostId(member.getId(), postId);
        return PostResponse.of(post, getProfile(member.getId()), isLiked);
    }

    public void delete(Long postId, Member member) {
        Post post = getOwnedPost(postId, member.getId());
        if (!post.getImageUrls().isEmpty()) {
            s3Service.deleteAll(post.getImageUrls());
        }
        commentRepository.clearSelfReferencesByPostId(postId);
        commentRepository.deleteAllByPostId(postId);
        postLikeRepository.deleteAllByPostId(postId);
        postRepository.delete(post);
    }

    public boolean toggleLike(Long postId, Member member) {
        Post post = getPost(postId);
        if (postLikeRepository.existsByMemberIdAndPostId(member.getId(), postId)) {
            postLikeRepository.deleteByMemberIdAndPostId(member.getId(), postId);
            post.decreaseLikeCount();
            return false;
        }
        postLikeRepository.save(PostLike.of(member, post));
        post.increaseLikeCount();
        return true;
    }

    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    private Profile getProfile(Long memberId) {
        return profileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }

    private Post getOwnedPost(Long postId, Long memberId) {
        Post post = getPost(postId);
        if (!post.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return post;
    }
}
