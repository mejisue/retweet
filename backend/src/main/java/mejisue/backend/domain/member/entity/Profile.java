package mejisue.backend.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mejisue.backend.common.entity.BaseTimeEntity;

@Entity
@Table(name = "profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(nullable = false)
    private String nickname;

    private String avatarUrl;

    @Column(length = 500)
    private String bio;

    @Builder(access = AccessLevel.PRIVATE)
    private Profile(Member member, String nickname, String avatarUrl) {
        this.member = member;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
    }

    public static Profile defaultOf(Member member) {
        String defaultNickname = member.getEmail().split("@")[0];
        return Profile.builder()
                .member(member)
                .nickname(defaultNickname)
                .build();
    }

    public static Profile of(Member member, String nickname, String avatarUrl) {
        return Profile.builder()
                .member(member)
                .nickname(nickname)
                .avatarUrl(avatarUrl)
                .build();
    }

    public void update(String nickname, String avatarUrl, String bio) {
        if (nickname != null && !nickname.isBlank()) this.nickname = nickname;
        if (avatarUrl != null) this.avatarUrl = avatarUrl;
        if (bio != null) this.bio = bio;
    }
}
