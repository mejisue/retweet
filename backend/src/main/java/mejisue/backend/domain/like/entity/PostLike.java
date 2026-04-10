package mejisue.backend.domain.like.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mejisue.backend.common.entity.BaseTimeEntity;
import mejisue.backend.domain.member.entity.Member;
import mejisue.backend.domain.post.entity.Post;

@Entity
@Table(name = "post_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends BaseTimeEntity {

    @EmbeddedId
    private PostLikeId id;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    private PostLike(Member member, Post post) {
        this.id = new PostLikeId(member.getId(), post.getId());
        this.member = member;
        this.post = post;
    }

    public static PostLike of(Member member, Post post) {
        return new PostLike(member, post);
    }
}
