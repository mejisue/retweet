package mejisue.backend.domain.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mejisue.backend.common.exception.BusinessException;
import mejisue.backend.common.exception.ErrorCode;
import mejisue.backend.domain.member.entity.Member;
import mejisue.backend.domain.profile.dto.ProfileResponse;
import mejisue.backend.domain.profile.dto.ProfileUpdateRequest;
import mejisue.backend.domain.profile.service.ProfileService;
import mejisue.backend.security.CustomUserDetails;
import mejisue.backend.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@TestPropertySource(properties = {
        "app.cookie.secure=false",
        "refresh-token.ttl-seconds=1209600",
        "app.frontend-url=http://localhost:3000"
})
class ProfileControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ProfileService profileService;
    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;

    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> {
            FilterChain chain = inv.getArgument(2);
            chain.doFilter(inv.getArgument(0, HttpServletRequest.class),
                    inv.getArgument(1, HttpServletResponse.class));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        Member member = Member.ofLocal("test@example.com", "encoded");
        setField(member, "id", 1L);
        CustomUserDetails userDetails = new CustomUserDetails(member);
        auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private ProfileResponse mockProfileResponse() {
        return new ProfileResponse(1L, "닉네임", "https://avatar.url", "소개글");
    }

    // ────────────────────────────────────────────
    // GET /api/profiles/me
    // ────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/profiles/me → 200, 내 프로필 반환")
    void getMyProfile_returns200() throws Exception {
        given(profileService.getProfile(1L)).willReturn(mockProfileResponse());

        mockMvc.perform(get("/api/profiles/me").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(1))
                .andExpect(jsonPath("$.nickname").value("닉네임"))
                .andExpect(jsonPath("$.avatarUrl").value("https://avatar.url"))
                .andExpect(jsonPath("$.bio").value("소개글"));
    }

    @Test
    @DisplayName("GET /api/profiles/me → avatarUrl이 null이어도 정상 반환")
    void getMyProfile_nullAvatar_returns200() throws Exception {
        given(profileService.getProfile(1L))
                .willReturn(new ProfileResponse(1L, "닉네임", null, null));

        mockMvc.perform(get("/api/profiles/me").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").isEmpty());
    }

    @Test
    @DisplayName("GET /api/profiles/me → 인증 없으면 401")
    void getMyProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/profiles/me"))
                .andExpect(status().isUnauthorized());
    }

    // ────────────────────────────────────────────
    // GET /api/profiles/{userId}
    // ────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/profiles/{userId} → 200, 특정 유저 프로필 반환")
    void getProfile_returns200() throws Exception {
        given(profileService.getProfile(2L))
                .willReturn(new ProfileResponse(2L, "다른유저", null, "소개"));

        mockMvc.perform(get("/api/profiles/2").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(2))
                .andExpect(jsonPath("$.nickname").value("다른유저"));
    }

    @Test
    @DisplayName("GET /api/profiles/{userId} → 존재하지 않는 유저 → 404")
    void getProfile_notFound_returns404() throws Exception {
        given(profileService.getProfile(999L))
                .willThrow(new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        mockMvc.perform(get("/api/profiles/999").with(authentication(auth)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/profiles/{userId} → 인증 없으면 401")
    void getProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/profiles/1"))
                .andExpect(status().isUnauthorized());
    }

    // ────────────────────────────────────────────
    // PUT /api/profiles/me
    // ────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/profiles/me → 200, 프로필 수정 성공")
    void updateMyProfile_returns200() throws Exception {
        ProfileResponse updated = new ProfileResponse(1L, "새닉네임", "https://new.url", "새소개");
        given(profileService.updateMyProfile(eq(1L), any())).willReturn(updated);

        ProfileUpdateRequest request = new ProfileUpdateRequest("새닉네임", "https://new.url", "새소개");

        mockMvc.perform(put("/api/profiles/me")
                        .with(authentication(auth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("새닉네임"))
                .andExpect(jsonPath("$.avatarUrl").value("https://new.url"))
                .andExpect(jsonPath("$.bio").value("새소개"));
    }

    @Test
    @DisplayName("PUT /api/profiles/me → avatarUrl null로 수정 가능")
    void updateMyProfile_nullAvatar_returns200() throws Exception {
        given(profileService.updateMyProfile(eq(1L), any()))
                .willReturn(new ProfileResponse(1L, "새닉네임", null, "소개"));

        ProfileUpdateRequest request = new ProfileUpdateRequest("새닉네임", null, "소개");

        mockMvc.perform(put("/api/profiles/me")
                        .with(authentication(auth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").isEmpty());
    }

    @Test
    @DisplayName("PUT /api/profiles/me → 인증 없으면 401")
    void updateMyProfile_unauthenticated_returns401() throws Exception {
        ProfileUpdateRequest request = new ProfileUpdateRequest("닉네임", null, null);

        mockMvc.perform(put("/api/profiles/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ────────────────────────────────────────────
    // Helper
    // ────────────────────────────────────────────

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
