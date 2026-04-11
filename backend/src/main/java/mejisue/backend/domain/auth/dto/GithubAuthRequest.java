package mejisue.backend.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GithubAuthRequest(
        @NotBlank
        String code
) {
}
