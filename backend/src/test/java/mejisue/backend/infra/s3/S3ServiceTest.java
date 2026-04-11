package mejisue.backend.infra.s3;

import mejisue.backend.common.exception.BusinessException;
import mejisue.backend.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock S3Client s3Client;

    @InjectMocks S3Service s3Service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucket", "test-bucket");
        ReflectionTestUtils.setField(s3Service, "cloudFrontDomain", "test.cloudfront.net");
    }

    // ────────────────────────────────────────────
    // upload
    // ────────────────────────────────────────────

    @Test
    @DisplayName("이미지 업로드 성공 → CloudFront URL 반환")
    void upload_success() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "image-content".getBytes());

        String url = s3Service.upload(file, 1L);

        assertThat(url).startsWith("https://test.cloudfront.net/posts/1/");
        assertThat(url).endsWith(".jpg");
        then(s3Client).should().putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("확장자 없는 파일 업로드 성공 → 확장자 없이 URL 반환")
    void upload_noExtension_success() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo", "image/jpeg", "image-content".getBytes());

        String url = s3Service.upload(file, 1L);

        assertThat(url).startsWith("https://test.cloudfront.net/posts/1/");
        then(s3Client).should().putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("허용되지 않는 Content-Type → INVALID_IMAGE_TYPE 예외")
    void upload_invalidContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", "content".getBytes());

        assertThatThrownBy(() -> s3Service.upload(file, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_IMAGE_TYPE);

        then(s3Client).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("S3 업로드 중 IOException → IMAGE_UPLOAD_FAILED 예외")
    void upload_ioException() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", "content".getBytes()) {
            @Override
            public java.io.InputStream getInputStream() throws IOException {
                throw new IOException("S3 error");
            }
        };

        assertThatThrownBy(() -> s3Service.upload(file, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.IMAGE_UPLOAD_FAILED);
    }

    @Test
    @DisplayName("지원하는 모든 이미지 타입 업로드 성공")
    void upload_allAllowedTypes() {
        for (String contentType : List.of("image/jpeg", "image/png", "image/gif", "image/webp")) {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "img.jpg", contentType, "content".getBytes());
            assertThat(s3Service.upload(file, 1L)).startsWith("https://test.cloudfront.net/");
        }
    }

    // ────────────────────────────────────────────
    // delete
    // ────────────────────────────────────────────

    @Test
    @DisplayName("CloudFront URL → key 추출 후 S3 삭제 호출")
    void delete_success() {
        String url = "https://test.cloudfront.net/posts/1/uuid.jpg";

        s3Service.delete(url);

        then(s3Client).should().deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("deleteAll → 각 URL에 대해 삭제 호출")
    void deleteAll_callsDeleteForEach() {
        List<String> urls = List.of(
                "https://test.cloudfront.net/posts/1/a.jpg",
                "https://test.cloudfront.net/posts/1/b.jpg",
                "https://test.cloudfront.net/posts/1/c.png"
        );

        s3Service.deleteAll(urls);

        then(s3Client).should(times(3)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("deleteAll 빈 리스트 → S3 호출 없음")
    void deleteAll_emptyList_noCall() {
        s3Service.deleteAll(List.of());

        then(s3Client).shouldHaveNoInteractions();
    }
}
