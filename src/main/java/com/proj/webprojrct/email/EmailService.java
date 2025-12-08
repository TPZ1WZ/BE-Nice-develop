package com.proj.webprojrct.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class EmailService {

    private final JavaMailSender mailSender;
    private final static Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Async
    public void send(String to, String userName, long token) {
        String emailContent = buildEmail(userName, token, to);
        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setText(emailContent, true);

            helper.setTo(to);

            helper.setSubject("Reset your password");
            helper.setFrom("no-reply@webhub.local");

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {

            LOGGER.error("failed to send email", e);

            throw new IllegalStateException("failed to send email");
        }
    }

    public String buildEmail(String name, long token, String email) {
        String verificationUrl = "http://localhost:8080/api/v1/auth/reset-password/" + token + "," + email;

        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n"
                + "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n"
                + "<table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n"
                + "    <tbody><tr>\n"
                + "        <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n"
                + "            <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n"
                + "                <tbody><tr>\n"
                + "                    <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n"
                + "                        <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n"
                + "                            <tbody><tr>\n"
                + "                                <td style=\"padding-left:10px\"></td>\n"
                + "                                <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n"
                + "                                    <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Đặt lại mật khẩu</span>\n"
                + "                                </td>\n"
                + "                            </tr>\n"
                + "                        </tbody></table>\n"
                + "                    </td>\n"
                + "                </tr>\n"
                + "            </tbody></table>\n"
                + "        </td>\n"
                + "    </tr>\n"
                + "</tbody></table>\n"
                + "<table role=\"presentation\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n"
                + "    <tbody><tr>\n"
                + "        <td height=\"30\"><br></td>\n"
                + "    </tr>\n"
                + "    <tr>\n"
                + "        <td width=\"10\" valign=\"middle\"><br></td>\n"
                + "        <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n"
                + "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Chào " + name + ",</p>\n"
                + "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng nhấn vào liên kết bên dưới để tạo mật khẩu mới cho tài khoản của bạn:</p>\n"
                + "            <p style=\"Margin:0 0 20px 0;text-align:center;\">\n"
                + "                <a href=\"" + verificationUrl + "\" style=\"font-size:22px;font-weight:bold;color:#1D70B8;text-decoration:none;\">"
                + "Đặt lại mật khẩu</a>\n"
                + "            </p>\n"
                + "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Liên kết này sẽ hết hạn sau 15 phút.</p>\n"
                + "            <p>Trân trọng,<br>Đội ngũ hỗ trợ</p>\n"
                + "        </td>\n"
                + "        <td width=\"10\" valign=\"middle\"><br></td>\n"
                + "    </tr>\n"
                + "    <tr>\n"
                + "        <td height=\"30\"><br></td>\n"
                + "    </tr>\n"
                + "</tbody></table>\n"
                + "<div class=\"yj6qo\"></div><div class=\"adL\"></div></div>";

    }

}
