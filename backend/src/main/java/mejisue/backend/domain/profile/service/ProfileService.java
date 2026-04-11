package mejisue.backend.domain.profile.service;

import lombok.RequiredArgsConstructor;
import mejisue.backend.common.exception.BusinessException;
import mejisue.backend.common.exception.ErrorCode;
import mejisue.backend.domain.profile.dto.ProfileResponse;
import mejisue.backend.domain.profile.dto.ProfileUpdateCommand;
import mejisue.backend.domain.profile.entity.Profile;
import mejisue.backend.domain.profile.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long memberId) {
        return profileRepository.findByMemberId(memberId)
                .map(ProfileResponse::of)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }

    public ProfileResponse updateMyProfile(Long memberId, ProfileUpdateCommand command) {
        Profile profile = profileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        profile.update(command.nickname(), command.avatarUrl(), command.bio());
        return ProfileResponse.of(profile);
    }
}
