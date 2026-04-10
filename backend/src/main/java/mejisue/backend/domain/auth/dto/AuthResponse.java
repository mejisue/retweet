package mejisue.backend.domain.auth.dto;

import mejisue.backend.domain.member.entity.Member;

public record AuthResponse(
        String accessToken,
        Long memberId,
        String email,
        String provider
) {
    public static AuthResponse of(String accessToken, Member member) {
        return new AuthResponse(
                accessToken,
                member.getId(),
                member.getEmail(),
                member.getProvider().name()
        );
    }
}
