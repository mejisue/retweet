package mejisue.backend.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LocalLoginRequest(
        @NotBlank @Email
        String email,

        @NotBlank
        String password
) {
}
