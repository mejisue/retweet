package mejisue.backend.domain.member.service;

import mejisue.backend.common.exception.BusinessException;
import mejisue.backend.common.exception.ErrorCode;
import mejisue.backend.domain.member.dto.ForgotPasswordRequest;
import mejisue.backend.domain.member.dto.GithubAuthRequest;
import mejisue.backend.domain.member.dto.LocalLoginRequest;
import mejisue.backend.domain.member.dto.LocalSignupRequest;
import mejisue.backend.domain.member.dto.ResetPasswordRequest;
import mejisue.backend.domain.member.entity.Member;
import mejisue.backend.domain.member.entity.PasswordResetToken;
import mejisue.backend.domain.member.repository.MemberRepository;
import mejisue.backend.domain.member.repository.PasswordResetTokenRepository;
import mejisue.backend.domain.member.repository.ProfileRepository;
import mejisue.backend.infra.email.EmailService;
import mejisue.backend.infra.github.GithubOAuthClient;
import mejisue.backend.infra.github.GithubUserInfo;
import mejisue.backend.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock ProfileRepository profileRepository;
    @Mock RefreshTokenStore refreshTokenStore;
    @Mock PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock GithubOAuthClient githubOAuthClient;
    @Mock EmailService emailService;

    @InjectMocks AuthService authService;

    // ────────────────────────────────────────────
    // 회원가입
    // ────────────────────────────────────────────

    @Test
    @DisplayName("이메일 회원가입 성공")
    void signup_success() {
        LocalSignupRequest request = new LocalSignupRequest("new@example.com", "password123");
        Member member = Member.ofLocal("new@example.com", "encoded");

        given(memberRepository.existsByEmail("new@example.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encoded");
        given(memberRepository.save(any())).willReturn(member);
        given(jwtService.generateAccessToken(any())).willReturn("access-token");
        given(refreshTokenStore.save(any())).willReturn("refresh-token");

        AuthService.TokenResult result = authService.signup(request);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        then(profileRepository).should().save(any());
    }

    @Test
    @DisplayName("중복 이메일 회원가입 → DUPLICATE_EMAIL 예외")
    void signup_duplicateEmail_throwsException() {
        LocalSignupRequest request = new LocalSignupRequest("dup@example.com", "password123");
        given(memberRepository.existsByEmail("dup@example.com")).willReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        then(memberRepository).should(never()).save(any());
    }

    // ────────────────────────────────────────────
    // 로그인
    // ────────────────────────────────────────────

    @Test
    @DisplayName("이메일 로그인 성공")
    void login_success() {
        LocalLoginRequest request = new LocalLoginRequest("user@example.com", "password123");
        Member member = Member.ofLocal("user@example.com", "encoded");

        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("password123", "encoded")).willReturn(true);
        given(jwtService.generateAccessToken(any())).willReturn("access-token");
        given(refreshTokenStore.save(any())).willReturn("refresh-token");

        AuthService.TokenResult result = authService.login(request);

        assertThat(result.accessToken()).isEqualTo("access-token");
    }

    @Test
    @DisplayName("존재하지 않는 이메일 → INVALID_CREDENTIALS 예외")
    void login_emailNotFound_throwsException() {
        LocalLoginRequest request = new LocalLoginRequest("none@example.com", "password123");
        given(memberRepository.findByEmail("none@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("비밀번호 불일치 → INVALID_CREDENTIALS 예외")
    void login_wrongPassword_throwsException() {
        LocalLoginRequest request = new LocalLoginRequest("user@example.com", "wrong");
        Member member = Member.ofLocal("user@example.com", "encoded");

        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("wrong", "encoded")).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    // ────────────────────────────────────────────
    // GitHub 인증
    // ────────────────────────────────────────────

    @Test
    @DisplayName("GitHub 신규 유저 → 회원 생성 후 토큰 발급")
    void authWithGithub_newUser_createsAndReturnsTokens() {
        GithubAuthRequest request = new GithubAuthRequest("code123");
        GithubUserInfo userInfo = new GithubUserInfo(99L, "octocat", "octo@github.com", "https://avatar.url");
        Member newMember = Member.ofGithub("99", "octo@github.com");

        given(githubOAuthClient.getAccessToken("code123")).willReturn("github-token");
        given(githubOAuthClient.getUserInfo("github-token")).willReturn(userInfo);
        given(memberRepository.findByGithubId("99")).willReturn(Optional.empty());
        given(memberRepository.save(any())).willReturn(newMember);
        given(jwtService.generateAccessToken(any())).willReturn("access-token");
        given(refreshTokenStore.save(any())).willReturn("refresh-token");

        AuthService.TokenResult result = authService.authWithGithub(request);

        assertThat(result.accessToken()).isEqualTo("access-token");
        then(profileRepository).should().save(any());
    }

    @Test
    @DisplayName("GitHub 기존 유저 → 프로필 생성 없이 토큰 발급")
    void authWithGithub_existingUser_returnsTokensWithoutCreatingProfile() {
        GithubAuthRequest request = new GithubAuthRequest("code123");
        GithubUserInfo userInfo = new GithubUserInfo(99L, "octocat", "octo@github.com", "https://avatar.url");
        Member existingMember = Member.ofGithub("99", "octo@github.com");

        given(githubOAuthClient.getAccessToken("code123")).willReturn("github-token");
        given(githubOAuthClient.getUserInfo("github-token")).willReturn(userInfo);
        given(memberRepository.findByGithubId("99")).willReturn(Optional.of(existingMember));
        given(jwtService.generateAccessToken(any())).willReturn("access-token");
        given(refreshTokenStore.save(any())).willReturn("refresh-token");

        authService.authWithGithub(request);

        then(memberRepository).should(never()).save(any());
        then(profileRepository).should(never()).save(any());
    }

    // ────────────────────────────────────────────
    // 토큰 재발급
    // ────────────────────────────────────────────

    @Test
    @DisplayName("refresh token 없음 → REFRESH_TOKEN_NOT_FOUND 예외")
    void refresh_nullToken_throwsException() {
        assertThatThrownBy(() -> authService.refresh(null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않거나 만료된 refresh token → REFRESH_TOKEN_NOT_FOUND 예외")
    void refresh_tokenNotFound_throwsException() {
        given(refreshTokenStore.findMemberIdByToken("unknown-token")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("unknown-token"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    @Test
    @DisplayName("유효한 refresh token → 토큰 로테이션 후 새 토큰 발급")
    void refresh_validToken_rotatesAndReturnsNewTokens() {
        Member member = Member.ofLocal("user@example.com", "encoded");
        setMemberId(member, 1L);

        given(refreshTokenStore.findMemberIdByToken("old-token")).willReturn(Optional.of(1L));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(jwtService.generateAccessToken(any())).willReturn("new-access-token");
        given(refreshTokenStore.save(any())).willReturn("new-refresh-token");

        AuthService.TokenResult result = authService.refresh("old-token");

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
        then(refreshTokenStore).should().delete("old-token");
    }

    // ────────────────────────────────────────────
    // 비밀번호 재설정
    // ────────────────────────────────────────────

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송 성공")
    void forgotPassword_existingEmail_sendsEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("user@example.com");
        Member member = Member.ofLocal("user@example.com", "encoded");

        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));
        given(passwordResetTokenRepository.save(any())).willReturn(null);

        authService.forgotPassword(request);

        then(emailService).should().sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("존재하지 않는 이메일 → 이메일 발송 없이 정상 종료 (이메일 존재 여부 비노출)")
    void forgotPassword_nonExistentEmail_doesNothing() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("none@example.com");
        given(memberRepository.findByEmail("none@example.com")).willReturn(Optional.empty());

        authService.forgotPassword(request);

        then(emailService).should(never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("만료된 비밀번호 재설정 토큰 → PASSWORD_RESET_TOKEN_EXPIRED 예외")
    void resetPassword_expiredToken_throwsException() {
        Member member = Member.ofLocal("user@example.com", "encoded");
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .member(member)
                .token("expired-reset-token")
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();

        ResetPasswordRequest request = new ResetPasswordRequest("expired-reset-token", "newPassword1");
        given(passwordResetTokenRepository.findByToken("expired-reset-token")).willReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED);
    }

    @Test
    @DisplayName("비밀번호 재설정 성공 → 모든 세션 무효화")
    void resetPassword_success_invalidatesAllSessions() {
        Member member = Member.ofLocal("user@example.com", "encoded");
        setMemberId(member, 1L);
        PasswordResetToken validToken = PasswordResetToken.builder()
                .member(member)
                .token("valid-reset-token")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        given(passwordResetTokenRepository.findByToken("valid-reset-token")).willReturn(Optional.of(validToken));
        given(passwordEncoder.encode("newPassword1")).willReturn("newEncoded");

        authService.resetPassword(new ResetPasswordRequest("valid-reset-token", "newPassword1"));

        then(refreshTokenStore).should().deleteAllByMemberId(1L);
        then(passwordResetTokenRepository).should().delete(validToken);
    }

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
