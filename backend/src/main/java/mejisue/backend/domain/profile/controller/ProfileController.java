package mejisue.backend.domain.profile.controller;

import lombok.RequiredArgsConstructor;
import mejisue.backend.common.exception.BusinessException;
import mejisue.backend.common.exception.ErrorCode;
import mejisue.backend.domain.profile.dto.ProfileResponse;
import mejisue.backend.domain.profile.repository.ProfileRepository;
import mejisue.backend.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileRepository profileRepository;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                profileRepository.findByMemberId(userDetails.getMember().getId())
                        .map(ProfileResponse::of)
                        .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND))
        );
    }
}
