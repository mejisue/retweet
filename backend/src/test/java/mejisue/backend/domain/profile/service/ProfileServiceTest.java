package mejisue.backend.domain.profile.service;

import mejisue.backend.common.exception.BusinessException;
import mejisue.backend.common.exception.ErrorCode;
import mejisue.backend.domain.member.entity.Member;
import mejisue.backend.domain.profile.dto.ProfileResponse;
import mejisue.backend.domain.profile.dto.ProfileUpdateCommand;
import mejisue.backend.domain.profile.entity.Profile;
import mejisue.backend.domain.profile.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProfileServiceTest {

    @Mock ProfileRepository profileRepository;

    @InjectMocks ProfileService profileService;

    private Member member;
    private Profile profile;

    @BeforeEach
    void setUp() {
        member = Member.ofLocal("test@example.com", "encoded");
        setField(member, "id", 1L);

        profile = mock(Profile.class);
        given(profile.getMember()).willReturn(member);
        given(profile.getNickname()).willReturn("닉네임");
        given(profile.getAvatarUrl()).willReturn("https://avatar.url");
        given(profile.getBio()).willReturn("소개글");
    }

    // ────────────────────────────────────────────
    // getProfile
    // ────────────────────────────────────────────

    @Test
    @DisplayName("getProfile - 정상 조회 성공")
    void getProfile_success() {
        given(profileRepository.findByMemberId(1L)).willReturn(Optional.of(profile));

        ProfileResponse result = profileService.getProfile(1L);

        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.nickname()).isEqualTo("닉네임");
        assertThat(result.avatarUrl()).isEqualTo("https://avatar.url");
        assertThat(result.bio()).isEqualTo("소개글");
    }

    @Test
    @DisplayName("getProfile - 존재하지 않는 멤버 → PROFILE_NOT_FOUND 예외")
    void getProfile_notFound_throwsException() {
        given(profileRepository.findByMemberId(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfile(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PROFILE_NOT_FOUND);
    }

    // ────────────────────────────────────────────
    // updateMyProfile
    // ────────────────────────────────────────────

    @Test
    @DisplayName("updateMyProfile - 닉네임/아바타/소개 모두 수정 성공")
    void updateMyProfile_allFields_success() {
        given(profileRepository.findByMemberId(1L)).willReturn(Optional.of(profile));

        ProfileUpdateCommand command = new ProfileUpdateCommand("새닉네임", "https://new.url", "새소개");
        profileService.updateMyProfile(1L, command);

        then(profile).should().update("새닉네임", "https://new.url", "새소개");
    }

    @Test
    @DisplayName("updateMyProfile - avatarUrl null이어도 수정 호출됨")
    void updateMyProfile_nullAvatar_callsUpdate() {
        given(profileRepository.findByMemberId(1L)).willReturn(Optional.of(profile));

        ProfileUpdateCommand command = new ProfileUpdateCommand("새닉네임", null, "새소개");
        profileService.updateMyProfile(1L, command);

        then(profile).should().update("새닉네임", null, "새소개");
    }

    @Test
    @DisplayName("updateMyProfile - 존재하지 않는 멤버 → PROFILE_NOT_FOUND 예외")
    void updateMyProfile_notFound_throwsException() {
        given(profileRepository.findByMemberId(999L)).willReturn(Optional.empty());

        ProfileUpdateCommand command = new ProfileUpdateCommand("닉네임", null, null);

        assertThatThrownBy(() -> profileService.updateMyProfile(999L, command))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PROFILE_NOT_FOUND);
    }

    @Test
    @DisplayName("updateMyProfile - 수정 후 반환값에 memberId 포함")
    void updateMyProfile_returnsProfileResponse() {
        given(profileRepository.findByMemberId(1L)).willReturn(Optional.of(profile));

        ProfileUpdateCommand command = new ProfileUpdateCommand("새닉네임", null, null);
        ProfileResponse result = profileService.updateMyProfile(1L, command);

        assertThat(result.memberId()).isEqualTo(1L);
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
