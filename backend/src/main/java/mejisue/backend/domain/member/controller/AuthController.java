package mejisue.backend.domain.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mejisue.backend.domain.member.dto.AuthResponse;
import mejisue.backend.domain.member.dto.ForgotPasswordRequest;
import mejisue.backend.domain.member.dto.GithubAuthRequest;
import mejisue.backend.domain.member.dto.LocalLoginRequest;
import mejisue.backend.domain.member.dto.LocalSignupRequest;
import mejisue.backend.domain.member.dto.ResetPasswordRequest;
import mejisue.backend.domain.member.service.AuthService;
import mejisue.backend.domain.member.service.AuthService.TokenResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @Value("${refresh-token.ttl-seconds}")
    private long refreshTokenTtlSeconds;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody @Valid LocalSignupRequest request) {
        TokenResult result = authService.signup(request);
        return buildAuthResponse(result, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LocalLoginRequest request) {
        TokenResult result = authService.login(request);
        return buildAuthResponse(result, HttpStatus.OK);
    }

    @PostMapping("/github")
    public ResponseEntity<AuthResponse> authWithGithub(@RequestBody @Valid GithubAuthRequest request) {
        TokenResult result = authService.authWithGithub(request);
        return buildAuthResponse(result, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {
        TokenResult result = authService.refresh(refreshToken);
        return buildAuthResponse(result, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildClearRefreshTokenCookie().toString())
                .build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<AuthResponse> buildAuthResponse(TokenResult result, HttpStatus status) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", result.refreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .maxAge(Duration.ofSeconds(refreshTokenTtlSeconds))
                .path("/api/auth")
                .build();

        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.authResponse());
    }

    private ResponseCookie buildClearRefreshTokenCookie() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .maxAge(0)
                .path("/api/auth")
                .build();
    }
}
