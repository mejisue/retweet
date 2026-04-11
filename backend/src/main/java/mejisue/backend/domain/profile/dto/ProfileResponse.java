package mejisue.backend.domain.profile.dto;

import mejisue.backend.domain.profile.entity.Profile;

public record ProfileResponse(
        Long memberId,
        String nickname,
        String avatarUrl,
        String bio
) {
    public static ProfileResponse of(Profile profile) {
        return new ProfileResponse(
                profile.getMember().getId(),
                profile.getNickname(),
                profile.getAvatarUrl(),
                profile.getBio()
        );
    }
}
