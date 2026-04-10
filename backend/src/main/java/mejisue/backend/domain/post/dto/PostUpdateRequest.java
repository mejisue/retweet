package mejisue.backend.domain.post.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PostUpdateRequest(
        @NotBlank
        String content,

        List<String> imageUrls
) {
}
