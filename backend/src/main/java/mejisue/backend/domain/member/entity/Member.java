package mejisue.backend.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mejisue.backend.common.entity.BaseTimeEntity;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(unique = true)
    private String githubId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder(access = AccessLevel.PRIVATE)
    private Member(String email, String password, String githubId, AuthProvider provider, Role role) {
        this.email = email;
        this.password = password;
        this.githubId = githubId;
        this.provider = provider;
        this.role = role;
    }

    // 이메일 가입: password는 반드시 인코딩된 값이어야 함
    public static Member ofLocal(String email, String encodedPassword) {
        return Member.builder()
                .email(email)
                .password(encodedPassword)
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();
    }

    // GitHub 가입: password 없이 생성, githubId 필수
    public static Member ofGithub(String githubId, String email) {
        return Member.builder()
                .githubId(githubId)
                .email(email)
                .provider(AuthProvider.GITHUB)
                .role(Role.USER)
                .build();
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
