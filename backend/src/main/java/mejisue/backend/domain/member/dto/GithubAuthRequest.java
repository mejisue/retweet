package mejisue.backend.domain.member.dto;

import jakarta.validation.constraints.NotBlank;

public record GithubAuthRequest(
        @NotBlank
        String code
) {
}
