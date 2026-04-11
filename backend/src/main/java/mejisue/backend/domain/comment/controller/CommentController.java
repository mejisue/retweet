package mejisue.backend.domain.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mejisue.backend.domain.comment.dto.CommentCreateRequest;
import mejisue.backend.domain.comment.dto.CommentResponse;
import mejisue.backend.domain.comment.dto.CommentUpdateRequest;
import mejisue.backend.domain.comment.service.CommentService;
import mejisue.backend.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.findAllByPostId(postId));
    }

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> create(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(postId, userDetails.getMember(), request));
    }

    @PutMapping("/api/comments/{commentId}")
    public ResponseEntity<CommentResponse> update(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CommentUpdateRequest request) {
        return ResponseEntity.ok(commentService.update(commentId, userDetails.getMember(), request));
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentService.delete(commentId, userDetails.getMember());
        return ResponseEntity.noContent().build();
    }
}
