package mejisue.backend.infra.email;

import lombok.RequiredArgsConstructor;
import mejisue.backend.common.exception.BusinessException;
import mejisue.backend.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String to, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("[미지 로그] 비밀번호 재설정");
        message.setText("""
                비밀번호 재설정을 요청하셨습니다.

                아래 링크를 클릭하여 비밀번호를 재설정하세요.
                링크는 1시간 후 만료됩니다.

                %s

                본인이 요청하지 않은 경우 이 메일을 무시하세요.
                """.formatted(resetLink));

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }
}
