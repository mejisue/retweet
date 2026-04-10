package mejisue.backend.infra.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubUserInfo(
        Long id,
        String login,
        String email,

        @JsonProperty("avatar_url")
        String avatarUrl
) {
}
