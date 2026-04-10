package mejisue.backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String MEMBER_REFRESH_PREFIX = "member_refresh:";

    private final StringRedisTemplate redisTemplate;

    @Value("${refresh-token.ttl-seconds}")
    private long ttlSeconds;

    /** 새 refresh token을 생성해 Redis에 저장하고 토큰 값을 반환한다. */
    public String save(Long memberId) {
        String token = UUID.randomUUID().toString();
        Duration ttl = Duration.ofSeconds(ttlSeconds);

        redisTemplate.opsForValue().set(REFRESH_PREFIX + token, String.valueOf(memberId), ttl);
        redisTemplate.opsForSet().add(MEMBER_REFRESH_PREFIX + memberId, token);
        redisTemplate.expire(MEMBER_REFRESH_PREFIX + memberId, ttl);

        return token;
    }

    /** 토큰으로 memberId를 조회한다. 존재하지 않거나 만료된 경우 empty를 반환한다. */
    public Optional<Long> findMemberIdByToken(String token) {
        String value = redisTemplate.opsForValue().get(REFRESH_PREFIX + token);
        if (value == null) return Optional.empty();
        return Optional.of(Long.parseLong(value));
    }

    /** 단일 refresh token을 삭제한다 (로그아웃, 토큰 로테이션). */
    public void delete(String token) {
        String memberId = redisTemplate.opsForValue().get(REFRESH_PREFIX + token);
        redisTemplate.delete(REFRESH_PREFIX + token);
        if (memberId != null) {
            redisTemplate.opsForSet().remove(MEMBER_REFRESH_PREFIX + memberId, token);
        }
    }

    /** 회원의 모든 refresh token을 삭제한다 (비밀번호 재설정 등 전체 세션 무효화). */
    public void deleteAllByMemberId(Long memberId) {
        Set<String> tokens = redisTemplate.opsForSet().members(MEMBER_REFRESH_PREFIX + memberId);
        if (tokens != null && !tokens.isEmpty()) {
            tokens.stream()
                    .map(t -> REFRESH_PREFIX + t)
                    .forEach(redisTemplate::delete);
        }
        redisTemplate.delete(MEMBER_REFRESH_PREFIX + memberId);
    }
}
