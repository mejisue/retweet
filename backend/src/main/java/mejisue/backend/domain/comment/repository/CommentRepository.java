package mejisue.backend.domain.comment.repository;

import mejisue.backend.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글의 최상위 댓글 + 대댓글을 한 번에 조회 (N+1 방지)
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.member WHERE c.post.id = :postId ORDER BY c.createdAt ASC")
    List<Comment> findAllByPostIdWithMember(@Param("postId") Long postId);

    // 자기 참조 FK(root_comment_id, parent_comment_id)를 먼저 NULL로 비워야 삭제 가능
    @Modifying
    @Query("UPDATE Comment c SET c.rootComment = null, c.parentComment = null WHERE c.post.id = :postId")
    void clearSelfReferencesByPostId(@Param("postId") Long postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.post.id = :postId")
    void deleteAllByPostId(@Param("postId") Long postId);
}
