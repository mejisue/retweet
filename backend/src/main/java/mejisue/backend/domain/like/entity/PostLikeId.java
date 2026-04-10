package mejisue.backend.domain.like.entity;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class PostLikeId implements Serializable {

    private Long memberId;
    private Long postId;

    public PostLikeId(Long memberId, Long postId) {
        this.memberId = memberId;
        this.postId = postId;
    }
}
