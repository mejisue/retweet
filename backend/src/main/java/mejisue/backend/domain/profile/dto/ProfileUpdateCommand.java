package mejisue.backend.domain.profile.dto;

public record ProfileUpdateCommand(
        String nickname,
        String avatarUrl,
        String bio
) {
}
