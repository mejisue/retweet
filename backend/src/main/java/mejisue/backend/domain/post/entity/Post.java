package mejisue.backend.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mejisue.backend.common.entity.BaseTimeEntity;
import mejisue.backend.domain.member.entity.Member;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ElementCollection
    @CollectionTable(name = "post_image_urls", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "image_url")
    @OrderColumn(name = "image_order")
    private List<String> imageUrls = new ArrayList<>();

    @Column(nullable = false)
    private int likeCount = 0;

    @Builder(access = AccessLevel.PRIVATE)
    private Post(Member member, String content, List<String> imageUrls) {
        this.member = member;
        this.content = content;
        if (imageUrls != null) this.imageUrls = imageUrls;
    }

    public static Post create(Member member, String content, List<String> imageUrls) {
        return Post.builder()
                .member(member)
                .content(content)
                .imageUrls(imageUrls)
                .build();
    }

    public void update(String content, List<String> imageUrls) {
        this.content = content;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }
}
