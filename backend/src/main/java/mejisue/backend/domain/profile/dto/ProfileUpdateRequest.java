package mejisue.backend.domain.profile.dto;

public record ProfileUpdateRequest(
        String nickname,
        String avatarUrl,
        String bio
) {
    public ProfileUpdateCommand toCommand() {
        return new ProfileUpdateCommand(nickname, avatarUrl, bio);
    }
}
