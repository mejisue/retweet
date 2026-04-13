package mejisue.backend.domain.post.dto;

import mejisue.backend.domain.post.entity.Post;
import mejisue.backend.domain.profile.entity.Profile;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Long id,
        String content,
        List<String> imageUrls,
        int likeCount,
        boolean isLiked,
        AuthorInfo author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record AuthorInfo(Long memberId, String nickname, String avatarUrl) {
    }

    public static PostResponse of(Post post, Profile profile, boolean isLiked) {
        return new PostResponse(
                post.getId(),
                post.getContent(),
                post.getImageUrls(),
                post.getLikeCount(),
                isLiked,
                new AuthorInfo(
                        post.getMember().getId(),
                        profile.getNickname(),
                        profile.getAvatarUrl()
                ),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
