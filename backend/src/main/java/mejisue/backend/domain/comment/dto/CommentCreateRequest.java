package mejisue.backend.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
        @NotBlank String content,
        Long parentCommentId,
        Long rootCommentId
) {}
