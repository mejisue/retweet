package mejisue.backend.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mejisue.backend.domain.comment.dto.CommentCreateRequest;
import mejisue.backend.domain.comment.dto.CommentResponse;
import mejisue.backend.domain.comment.dto.CommentUpdateRequest;
import mejisue.backend.domain.comment.service.CommentService;
import mejisue.backend.domain.member.entity.Member;
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

@WebMvcTest(CommentController.class)
@TestPropertySource(properties = {
        "app.cookie.secure=false",
        "refresh-token.ttl-seconds=1209600",
        "app.frontend-url=http://localhost:3000"
})
class CommentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean CommentService commentService;
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

    private CommentResponse mockCommentResponse() {
        return new CommentResponse(
                1L, 1L, "댓글 내용",
                new CommentResponse.AuthorInfo(1L, "닉네임", null),
                null, null,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    // ────────────────────────────────────────────
    // GET /api/posts/{postId}/comments
    // ────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/posts/{postId}/comments → 200, 댓글 목록 반환")
    void getComments_returns200() throws Exception {
        given(commentService.findAllByPostId(1L)).willReturn(List.of(mockCommentResponse()));

        mockMvc.perform(get("/api/posts/1/comments").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].content").value("댓글 내용"))
                .andExpect(jsonPath("$[0].author.nickname").value("닉네임"))
                .andExpect(jsonPath("$[0].parentCommentId").doesNotExist())
                .andExpect(jsonPath("$[0].rootCommentId").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/posts/{postId}/comments → 댓글 없으면 빈 배열 반환")
    void getComments_empty_returns200() throws Exception {
        given(commentService.findAllByPostId(1L)).willReturn(List.of());

        mockMvc.perform(get("/api/posts/1/comments").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ────────────────────────────────────────────
    // POST /api/posts/{postId}/comments
    // ────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/posts/{postId}/comments → 201, 최상위 댓글 생성")
    void create_rootComment_returns201() throws Exception {
        given(commentService.create(eq(1L), any(), any())).willReturn(mockCommentResponse());

        mockMvc.perform(post("/api/posts/1/comments")
                        .with(authentication(auth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommentCreateRequest("댓글 내용", null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("댓글 내용"))
                .andExpect(jsonPath("$.author.nickname").value("닉네임"));
    }

    @Test
    @DisplayName("POST /api/posts/{postId}/comments → 201, 대댓글 생성")
    void create_reply_returns201() throws Exception {
        CommentResponse replyResponse = new CommentResponse(
                3L, 1L, "대댓글 내용",
                new CommentResponse.AuthorInfo(1L, "닉네임", null),
                2L, 2L,
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(commentService.create(eq(1L), any(), any())).willReturn(replyResponse);

        mockMvc.perform(post("/api/posts/1/comments")
                        .with(authentication(auth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommentCreateRequest("대댓글 내용", 2L, 2L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rootCommentId").value(2))
                .andExpect(jsonPath("$.parentCommentId").value(2));
    }

    @Test
    @DisplayName("POST /api/posts/{postId}/comments → content 없으면 400")
    void create_blankContent_returns400() throws Exception {
        mockMvc.perform(post("/api/posts/1/comments")
                        .with(authentication(auth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommentCreateRequest("", null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/posts/{postId}/comments → 인증 없으면 401")
    void create_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/posts/1/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommentCreateRequest("댓글 내용", null, null))))
                .andExpect(status().isUnauthorized());
    }

    // ────────────────────────────────────────────
    // PUT /api/comments/{commentId}
    // ────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/comments/{commentId} → 200, 댓글 수정")
    void update_returns200() throws Exception {
        CommentResponse updated = new CommentResponse(
                1L, 1L, "수정된 내용",
                new CommentResponse.AuthorInfo(1L, "닉네임", null),
                null, null,
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(commentService.update(eq(1L), any(), any())).willReturn(updated);

        mockMvc.perform(put("/api/comments/1")
                        .with(authentication(auth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommentUpdateRequest("수정된 내용"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정된 내용"));
    }

    @Test
    @DisplayName("PUT /api/comments/{commentId} → content 없으면 400")
    void update_blankContent_returns400() throws Exception {
        mockMvc.perform(put("/api/comments/1")
                        .with(authentication(auth)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommentUpdateRequest(""))))
                .andExpect(status().isBadRequest());
    }

    // ────────────────────────────────────────────
    // DELETE /api/comments/{commentId}
    // ────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/comments/{commentId} → 204, 댓글 삭제")
    void delete_returns204() throws Exception {
        willDoNothing().given(commentService).delete(eq(1L), any());

        mockMvc.perform(delete("/api/comments/1").with(authentication(auth)).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/comments/{commentId} → 인증 없으면 401")
    void delete_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/comments/1").with(csrf()))
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
