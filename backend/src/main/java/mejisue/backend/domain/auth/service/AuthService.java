package mejisue.backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import mejisue.backend.common.exception.BusinessException;
import mejisue.backend.common.exception.ErrorCode;
import mejisue.backend.domain.auth.dto.AuthResponse;
import mejisue.backend.domain.auth.dto.ForgotPasswordRequest;
import mejisue.backend.domain.auth.dto.GithubAuthRequest;
import mejisue.backend.domain.auth.dto.LocalLoginRequest;
import mejisue.backend.domain.auth.dto.LocalSignupRequest;
import mejisue.backend.domain.auth.dto.ResetPasswordRequest;
import mejisue.backend.domain.auth.entity.PasswordResetToken;
import mejisue.backend.domain.auth.repository.PasswordResetTokenRepository;
import mejisue.backend.domain.member.entity.Member;
import mejisue.backend.domain.member.repository.MemberRepository;
import mejisue.backend.domain.profile.entity.Profile;
import mejisue.backend.domain.profile.repository.ProfileRepository;
import mejisue.backend.infra.email.EmailService;
import mejisue.backend.infra.github.GithubOAuthClient;
import mejisue.backend.infra.github.GithubUserInfo;
import mejisue.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final RefreshTokenStore refreshTokenStore;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GithubOAuthClient githubOAuthClient;
    private final EmailService emailService;

    // 이메일 회원가입
    public TokenResult signup(LocalSignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = memberRepository.save(
                Member.ofLocal(request.email(), passwordEncoder.encode(request.password())));
        profileRepository.save(Profile.defaultOf(member));

        return generateTokens(member);
    }

    // 이메일 로그인
    @Transactional(readOnly = true)
    public TokenResult login(LocalLoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return generateTokens(member);
    }

    // GitHub 로그인 + 회원가입 통합 (find-or-create)
    public TokenResult authWithGithub(GithubAuthRequest request) {
        String accessToken = githubOAuthClient.getAccessToken(request.code());
        GithubUserInfo userInfo = githubOAuthClient.getUserInfo(accessToken);

        Member member = memberRepository.findByGithubId(String.valueOf(userInfo.id()))
                .orElseGet(() -> {
                    Member newMember = memberRepository.save(
                            Member.ofGithub(String.valueOf(userInfo.id()), userInfo.email()));
                    profileRepository.save(Profile.of(newMember, userInfo.login(), userInfo.avatarUrl()));
                    return newMember;
                });

        return generateTokens(member);
    }

    // Access token 재발급 (Refresh token rotation)
    public TokenResult refresh(String refreshTokenValue) {
        if (refreshTokenValue == null) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        Long memberId = refreshTokenStore.findMemberIdByToken(refreshTokenValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        refreshTokenStore.delete(refreshTokenValue);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return generateTokens(member);
    }

    // 로그아웃
    public void logout(String refreshTokenValue) {
        if (refreshTokenValue != null) {
            refreshTokenStore.delete(refreshTokenValue);
        }
    }

    // 비밀번호 재설정 이메일 발송
    public void forgotPassword(ForgotPasswordRequest request) {
        memberRepository.findByEmail(request.email()).ifPresent(member -> {
            passwordResetTokenRepository.deleteByMemberId(member.getId());

            String token = UUID.randomUUID().toString();
            passwordResetTokenRepository.save(PasswordResetToken.builder()
                    .member(member)
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .build());

            emailService.sendPasswordResetEmail(member.getEmail(), token);
        });
        // 이메일이 없어도 보안상 동일한 응답 (이메일 존재 여부 노출 방지)
    }

    // 비밀번호 재설정
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new BusinessException(ErrorCode.PASSWORD_RESET_TOKEN_NOT_FOUND));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new BusinessException(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED);
        }

        Member member = resetToken.getMember();
        member.updatePassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenStore.deleteAllByMemberId(member.getId()); // 기존 세션 모두 무효화
        passwordResetTokenRepository.delete(resetToken);
    }

    private TokenResult generateTokens(Member member) {
        String accessToken = jwtService.generateAccessToken(member);
        String refreshToken = refreshTokenStore.save(member.getId());
        return new TokenResult(accessToken, refreshToken, AuthResponse.of(accessToken, member));
    }

    public record TokenResult(String accessToken, String refreshToken, AuthResponse authResponse) {
    }
}
