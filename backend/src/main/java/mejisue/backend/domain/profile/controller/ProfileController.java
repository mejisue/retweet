package mejisue.backend.domain.profile.controller;

import lombok.RequiredArgsConstructor;
import mejisue.backend.domain.profile.dto.ProfileResponse;
import mejisue.backend.domain.profile.dto.ProfileUpdateRequest;
import mejisue.backend.domain.profile.service.ProfileService;
import mejisue.backend.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                profileService.getProfile(userDetails.getMember().getId())
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(
                profileService.updateMyProfile(userDetails.getMember().getId(), request.toCommand())
        );
    }
}
