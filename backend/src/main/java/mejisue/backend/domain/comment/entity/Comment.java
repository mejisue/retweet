package mejisue.backend.domain.comment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mejisue.backend.common.entity.BaseTimeEntity;
import mejisue.backend.domain.member.entity.Member;
import mejisue.backend.domain.post.entity.Post;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 최상위 댓글 (대댓글일 때만 값이 있음, 자신이 최상위면 null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_comment_id")
    private Comment rootComment;

    // 직접 부모 댓글 (대댓글일 때만 값이 있음)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Builder(access = AccessLevel.PRIVATE)
    private Comment(Post post, Member member, String content, Comment rootComment, Comment parentComment) {
        this.post = post;
        this.member = member;
        this.content = content;
        this.rootComment = rootComment;
        this.parentComment = parentComment;
    }

    // 최상위 댓글 생성
    public static Comment createRoot(Post post, Member member, String content) {
        return Comment.builder()
                .post(post)
                .member(member)
                .content(content)
                .build();
    }

    // 대댓글 생성
    public static Comment createReply(Post post, Member member, String content, Comment rootComment, Comment parentComment) {
        return Comment.builder()
                .post(post)
                .member(member)
                .content(content)
                .rootComment(rootComment)
                .parentComment(parentComment)
                .build();
    }

    public void update(String content) {
        this.content = content;
    }

    public boolean isRoot() {
        return this.rootComment == null;
    }
}
