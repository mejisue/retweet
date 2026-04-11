package mejisue.backend.domain.profile.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mejisue.backend.domain.member.entity.Member;
import mejisue.backend.domain.profile.entity.Profile;
import mejisue.backend.domain.profile.repository.ProfileRepository;
import mejisue.backend.security.CustomUserDetails;
import mejisue.backend.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@TestPropertySource(properties = {
        "app.cookie.secure=false",
        "refresh-token.ttl-seconds=1209600",
        "app.frontend-url=http://localhost:3000"
})
class ProfileControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean ProfileRepository profileRepository;
    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;

    private UsernamePasswordAuthenticationToken auth;
    private Member member;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> {
            FilterChain chain = inv.getArgument(2);
            chain.doFilter(inv.getArgument(0, HttpServletRequest.class),
                    inv.getArgument(1, HttpServletResponse.class));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        member = Member.ofLocal("test@example.com", "encoded");
        setField(member, "id", 1L);
        CustomUserDetails userDetails = new CustomUserDetails(member);
        auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    // ────────────────────────────────────────────
    // GET /api/profiles/me
    // ────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/profiles/me → 200, 내 프로필 반환")
    void getMyProfile_returns200() throws Exception {
        Profile profile = mock(Profile.class);
        given(profile.getMember()).willReturn(member);
        given(profile.getNickname()).willReturn("닉네임");
        given(profile.getAvatarUrl()).willReturn("https://avatar.url");
        given(profile.getBio()).willReturn("소개글");

        given(profileRepository.findByMemberId(1L)).willReturn(Optional.of(profile));

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
        Profile profile = mock(Profile.class);
        given(profile.getMember()).willReturn(member);
        given(profile.getNickname()).willReturn("닉네임");
        given(profile.getAvatarUrl()).willReturn(null);
        given(profile.getBio()).willReturn(null);

        given(profileRepository.findByMemberId(1L)).willReturn(Optional.of(profile));

        mockMvc.perform(get("/api/profiles/me").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(1))
                .andExpect(jsonPath("$.avatarUrl").isEmpty());
    }

    @Test
    @DisplayName("GET /api/profiles/me → 인증 없으면 401")
    void getMyProfile_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/profiles/me"))
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
