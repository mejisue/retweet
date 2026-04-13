package mejisue.backend.domain.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mejisue.backend.domain.member.entity.Member;
import mejisue.backend.domain.post.dto.PostCreateRequest;
import mejisue.backend.domain.post.dto.PostResponse;
import mejisue.backend.domain.post.dto.PostUpdateRequest;
import mejisue.backend.domain.post.service.PostService;
import mejisue.backend.security.CustomUserDetails;
import mejisue.backend.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@TestPropertySource(properties = {
        "app.cookie.secure=false",
        "refresh-token.ttl-seconds=1209600",
        "app.frontend-url=http://localhost:3000"
})
class PostControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean PostService postService;
    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;

    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() throws Exception {
        // JwtAuthenticationFilter mock이 필터 체인을 그냥 통과시키도록 설정
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

    private PostResponse mockPostResponse() {
        return new PostResponse(
                1L, "테스트 내용", List.of(), 0, false,
                new PostResponse.AuthorInfo(1L, "닉네임", null),
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    // ────────────────────────────────────────────
    // POST /api/posts
    // ────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/posts → 201, 게시글 생성")
    void create_returns201() throws Exception {
        given(postService.create(any(), any())).willReturn(mockPostResponse());

        mockMvc.perform(post("/api/posts")
                        .with(authentication(auth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PostCreateRequest("테스트 내용", List.of()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("테스트 내용"))
                .andExpect(jsonPath("$.author.nickname").value("닉네임"));
    }

    @Test
    @DisplayName("POST /api/posts → content 없으면 400")
    void create_blankContent_returns400() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .with(authentication(auth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PostCreateRequest("", List.of()))))
                .andExpect(status().isBadRequest());
    }

    // ────────────────────────────────────────────
    // GET /api/posts
    // ────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/posts → 200, 게시글 목록")
    void getAll_returns200() throws Exception {
        given(postService.findAll(any(), any())).willReturn(new SliceImpl<>(List.of(mockPostResponse())));

        mockMvc.perform(get("/api/posts").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].content").value("테스트 내용"))
                .andExpect(jsonPath("$.content[0].isLiked").value(false));
    }

    // ────────────────────────────────────────────
    // GET /api/posts/{id}
    // ────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/posts/{id} → 200, 단건 조회")
    void getById_returns200() throws Exception {
        given(postService.findById(eq(1L), any())).willReturn(mockPostResponse());

        mockMvc.perform(get("/api/posts/1").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("테스트 내용"))
                .andExpect(jsonPath("$.isLiked").value(false));
    }

    // ────────────────────────────────────────────
    // GET /api/posts/member/{memberId}
    // ────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/posts/member/{memberId} → 200, 회원별 게시글 목록")
    void getByMember_returns200() throws Exception {
        given(postService.findByMemberId(eq(1L), any(), any())).willReturn(new SliceImpl<>(List.of(mockPostResponse())));

        mockMvc.perform(get("/api/posts/member/1").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].isLiked").value(false));
    }

    // ────────────────────────────────────────────
    // PUT /api/posts/{id}
    // ────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/posts/{id} → 200, 게시글 수정")
    void update_returns200() throws Exception {
        PostResponse updated = new PostResponse(
                1L, "수정된 내용", List.of(), 0, false,
                new PostResponse.AuthorInfo(1L, "닉네임", null),
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(postService.update(eq(1L), any(), any())).willReturn(updated);

        mockMvc.perform(put("/api/posts/1")
                        .with(authentication(auth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PostUpdateRequest("수정된 내용", List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정된 내용"));
    }

    // ────────────────────────────────────────────
    // DELETE /api/posts/{id}
    // ────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/posts/{id} → 204, 게시글 삭제")
    void delete_returns204() throws Exception {
        willDoNothing().given(postService).delete(eq(1L), any());

        mockMvc.perform(delete("/api/posts/1").with(authentication(auth)).with(csrf()))
                .andExpect(status().isNoContent());
    }

    // ────────────────────────────────────────────
    // POST /api/posts/{id}/like
    // ────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/posts/{id}/like → 200, 좋아요 추가")
    void toggleLike_like_returns200() throws Exception {
        given(postService.toggleLike(eq(1L), any())).willReturn(true);

        mockMvc.perform(post("/api/posts/1/like").with(authentication(auth)).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true));
    }

    @Test
    @DisplayName("POST /api/posts/{id}/like → 200, 좋아요 취소")
    void toggleLike_unlike_returns200() throws Exception {
        given(postService.toggleLike(eq(1L), any())).willReturn(false);

        mockMvc.perform(post("/api/posts/1/like").with(authentication(auth)).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false));
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
