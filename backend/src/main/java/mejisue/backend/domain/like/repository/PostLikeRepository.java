package mejisue.backend.domain.like.repository;

import mejisue.backend.domain.like.entity.PostLike;
import mejisue.backend.domain.like.entity.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

    boolean existsByMemberIdAndPostId(Long memberId, Long postId);

    void deleteByMemberIdAndPostId(Long memberId, Long postId);
}
