package mejisue.backend.domain.member.repository;

import mejisue.backend.domain.member.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
