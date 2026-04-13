package mejisue.backend.domain.image.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mejisue.backend.domain.member.entity.Member;
import mejisue.backend.infra.s3.S3Service;
import mejisue.backend.security.CustomUserDetails;
import mejisue.backend.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImageController.class)
@TestPropertySource(properties = {
        "app.cookie.secure=false",
        "refresh-token.ttl-seconds=1209600",
        "app.frontend-url=http://localhost:3000"
})
class ImageControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean S3Service s3Service;
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

    // ────────────────────────────────────────────
    // POST /api/images
    // ────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/images → 200, CloudFront URL 목록 반환")
    void upload_returns200() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "files", "photo1.jpg", MediaType.IMAGE_JPEG_VALUE, "img1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "photo2.png", MediaType.IMAGE_PNG_VALUE, "img2".getBytes());

        given(s3Service.upload(any(), eq(1L)))
                .willReturn("https://test.cloudfront.net/posts/1/uuid1.jpg")
                .willReturn("https://test.cloudfront.net/posts/1/uuid2.png");

        mockMvc.perform(multipart("/api/images")
                        .file(file1)
                        .file(file2)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urls[0]").value("https://test.cloudfront.net/posts/1/uuid1.jpg"))
                .andExpect(jsonPath("$.urls[1]").value("https://test.cloudfront.net/posts/1/uuid2.png"))
                .andExpect(jsonPath("$.urls.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/images → 파일 1개 업로드 성공")
    void upload_singleFile_returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes());

        given(s3Service.upload(any(), eq(1L)))
                .willReturn("https://test.cloudfront.net/posts/1/uuid.jpg");

        mockMvc.perform(multipart("/api/images")
                        .file(file)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urls[0]").value("https://test.cloudfront.net/posts/1/uuid.jpg"))
                .andExpect(jsonPath("$.urls.length()").value(1));
    }

    @Test
    @DisplayName("POST /api/images → 인증 없으면 401")
    void upload_unauthenticated_returns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes());

        mockMvc.perform(multipart("/api/images")
                        .file(file)
                        .with(csrf()))
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
