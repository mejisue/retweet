package mejisue.backend.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mejisue.backend.domain.auth.dto.AuthResponse;
import mejisue.backend.domain.auth.dto.LocalLoginRequest;
import mejisue.backend.domain.auth.dto.LocalSignupRequest;
import mejisue.backend.domain.auth.service.AuthService;
import mejisue.backend.domain.auth.service.AuthService.TokenResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import mejisue.backend.security.JwtAuthenticationFilter;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "app.cookie.secure=false",
        "refresh-token.ttl-seconds=1209600",
        "app.frontend-url=http://localhost:3000"
})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;
    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;

    private TokenResult mockTokenResult() {
        AuthResponse authResponse = new AuthResponse("access-token", 1L, "test@example.com", "LOCAL");
        return new TokenResult("access-token", "refresh-token", authResponse);
    }

    @Test
    @DisplayName("POST /api/auth/signup → 201, accessToken 포함")
    void signup_returns201() throws Exception {
        given(authService.signup(any())).willReturn(mockTokenResult());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LocalSignupRequest("test@example.com", "password123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/signup → 이메일 형식 오류 시 400")
    void signup_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LocalSignupRequest("not-an-email", "password123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/signup → 비밀번호 8자 미만 시 400")
    void signup_shortPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LocalSignupRequest("test@example.com", "short"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login → 200, accessToken 포함")
    void login_returns200() throws Exception {
        given(authService.login(any())).willReturn(mockTokenResult());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LocalLoginRequest("test@example.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("POST /api/auth/logout → 200")
    void logout_returns200() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password → 200")
    void forgotPassword_returns200() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\"}"))
                .andExpect(status().isOk());
    }
}
