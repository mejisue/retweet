package mejisue.backend.infra.github;

import mejisue.backend.common.exception.BusinessException;
import mejisue.backend.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class GithubOAuthClient {

    private final RestClient restClient;

    @Value("${github.client-id}")
    private String clientId;

    @Value("${github.client-secret}")
    private String clientSecret;

    public GithubOAuthClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public String getAccessToken(String code) {
        try {
            Map<?, ?> response = restClient.post()
                    .uri("https://github.com/login/oauth/access_token")
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Map.of("client_id", clientId, "client_secret", clientSecret, "code", code))
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("access_token")) {
                throw new BusinessException(ErrorCode.GITHUB_AUTH_FAILED);
            }
            return (String) response.get("access_token");
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.GITHUB_AUTH_FAILED);
        }
    }

    public GithubUserInfo getUserInfo(String accessToken) {
        try {
            GithubUserInfo userInfo = restClient.get()
                    .uri("https://api.github.com/user")
                    .header("Authorization", "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(GithubUserInfo.class);

            if (userInfo == null) throw new BusinessException(ErrorCode.GITHUB_AUTH_FAILED);
            return userInfo;
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.GITHUB_AUTH_FAILED);
        }
    }
}
