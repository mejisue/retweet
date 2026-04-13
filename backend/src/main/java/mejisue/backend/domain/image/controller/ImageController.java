package mejisue.backend.domain.image.controller;

import lombok.RequiredArgsConstructor;
import mejisue.backend.domain.image.dto.ImageUploadResponse;
import mejisue.backend.infra.s3.S3Service;
import mejisue.backend.security.CustomUserDetails;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final S3Service s3Service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> upload(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("files") List<MultipartFile> files) {
        Long memberId = userDetails.getMember().getId();
        List<String> urls = files.stream()
                .map(file -> s3Service.upload(file, memberId))
                .toList();
        return ResponseEntity.ok(new ImageUploadResponse(urls));
    }
}
