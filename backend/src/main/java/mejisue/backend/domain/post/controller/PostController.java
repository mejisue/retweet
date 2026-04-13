package mejisue.backend.domain.post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mejisue.backend.domain.post.dto.PostCreateRequest;
import mejisue.backend.domain.post.dto.PostResponse;
import mejisue.backend.domain.post.dto.PostUpdateRequest;
import mejisue.backend.domain.post.service.PostService;
import mejisue.backend.security.CustomUserDetails;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PostCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.create(userDetails.getMember(), request));
    }

    @GetMapping
    public ResponseEntity<Slice<PostResponse>> getAll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.findAll(userDetails.getMember().getId(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.findById(id, userDetails.getMember().getId()));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<Slice<PostResponse>> getByMember(
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.findByMemberId(memberId, userDetails.getMember().getId(), pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> update(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PostUpdateRequest request) {
        return ResponseEntity.ok(postService.update(id, userDetails.getMember(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        postService.delete(id, userDetails.getMember());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Boolean>> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean liked = postService.toggleLike(id, userDetails.getMember());
        return ResponseEntity.ok(Map.of("liked", liked));
    }
}
