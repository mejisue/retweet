package mejisue.backend.domain.comment.service;

import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;

    public CommentResponse create(Long postId, Member member, CommentCreateRequest request) {
        Post post = getPost(postId);
        Comment comment;

        if (request.rootCommentId() != null) {
            Comment rootComment = getComment(request.rootCommentId());
            Comment parentComment = getComment(request.parentCommentId());
            comment = Comment.createReply(post, member, request.content(), rootComment, parentComment);
        } else {
            comment = Comment.createRoot(post, member, request.content());
        }

        Comment saved = commentRepository.save(comment);
        Profile profile = getProfile(member.getId());
        return CommentResponse.of(saved, profile);
    }

    public CommentResponse update(Long commentId, Member member, CommentUpdateRequest request) {
        Comment comment = getOwnedComment(commentId, member.getId());
        comment.update(request.content());
        Profile profile = getProfile(member.getId());
        return CommentResponse.of(comment, profile);
    }

    public void delete(Long commentId, Member member) {
        Comment comment = getOwnedComment(commentId, member.getId());
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> findAllByPostId(Long postId) {
        return commentRepository.findAllByPostIdWithMember(postId).stream()
                .map(comment -> CommentResponse.of(comment, getProfile(comment.getMember().getId())))
                .toList();
    }

    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private Profile getProfile(Long memberId) {
        return profileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }

    private Comment getOwnedComment(Long commentId, Long memberId) {
        Comment comment = getComment(commentId);
        if (!comment.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return comment;
    }
}
