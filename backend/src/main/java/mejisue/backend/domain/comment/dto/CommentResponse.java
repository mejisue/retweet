package mejisue.backend.domain.comment.dto;

import mejisue.backend.domain.comment.entity.Comment;
import mejisue.backend.domain.profile.entity.Profile;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long postId,
        String content,
        AuthorInfo author,
        Long parentCommentId,
        Long rootCommentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record AuthorInfo(Long memberId, String nickname, String avatarUrl) {}

    public static CommentResponse of(Comment comment, Profile profile) {
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getContent(),
                new AuthorInfo(
                        comment.getMember().getId(),
                        profile.getNickname(),
                        profile.getAvatarUrl()
                ),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                comment.getRootComment() != null ? comment.getRootComment().getId() : null,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
