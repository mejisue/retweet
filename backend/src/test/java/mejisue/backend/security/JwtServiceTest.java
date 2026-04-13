package mejisue.backend.security;

import mejisue.backend.common.exception.BusinessException;
import mejisue.backend.common.exception.ErrorCode;
import mejisue.backend.domain.member.entity.AuthProvider;
import mejisue.backend.domain.member.entity.Member;
import mejisue.backend.domain.member.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm");
        props.setAccessTokenExpiration(900_000L);
        jwtService = new JwtService(props);
    }

    @Test
    @DisplayName("access token 생성 후 memberId 추출 성공")
    void generateAccessToken_extractMemberId_success() {
        Member member = Member.ofLocal("test@example.com", "encodedPassword");
        setMemberId(member, 1L);

        String token = jwtService.generateAccessToken(member);
        Long memberId = jwtService.extractMemberId(token);

        assertThat(memberId).isEqualTo(1L);
    }

    @Test
    @DisplayName("잘못된 토큰 → INVALID_TOKEN 예외")
    void extractMemberId_invalidToken_throwsException() {
        assertThatThrownBy(() -> jwtService.extractMemberId("invalid.token.value"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("만료된 토큰 → EXPIRED_TOKEN 예외")
    void extractMemberId_expiredToken_throwsException() {
        JwtProperties expiredProps = new JwtProperties();
        expiredProps.setSecret("test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm");
        expiredProps.setAccessTokenExpiration(-1000L); // 이미 만료
        JwtService expiredJwtService = new JwtService(expiredProps);

        Member member = Member.ofLocal("test@example.com", "encodedPassword");
        setMemberId(member, 1L);
        String expiredToken = expiredJwtService.generateAccessToken(member);

        assertThatThrownBy(() -> jwtService.extractMemberId(expiredToken))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.EXPIRED_TOKEN);
    }

    // 테스트용 id 주입 (reflection)
    private void setMemberId(Member member, Long id) {
        try {
            var field = Member.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(member, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
