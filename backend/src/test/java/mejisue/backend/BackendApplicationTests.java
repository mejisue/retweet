package mejisue.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

    @MockBean
    JavaMailSender javaMailSender;

    @MockBean
    StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {
    }
}
