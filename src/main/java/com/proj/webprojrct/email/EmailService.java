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
                + "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Chào "
                + name + ",</p>\n"
                + "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng nhấn vào liên kết bên dưới để tạo mật khẩu mới cho tài khoản của bạn:</p>\n"
                + "            <p style=\"Margin:0 0 20px 0;text-align:center;\">\n"
                + "                <a href=\"" + verificationUrl
                + "\" style=\"font-size:22px;font-weight:bold;color:#1D70B8;text-decoration:none;\">"
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

    @Async
    public void sendRegistrationOtp(String to, String userName, long otp) {
        String emailContent = buildRegistrationOtpEmail(userName, otp);
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(emailContent, true);
            helper.setTo(to);
            helper.setSubject("Mã OTP xác thực đăng ký - Nike Store");
            helper.setFrom("no-reply@nikestore.local");
            mailSender.send(mimeMessage);
            LOGGER.info("✅ OTP email sent successfully to: {}", to);
        } catch (MessagingException e) {
            LOGGER.error("❌ Failed to send OTP email to: {}", to, e);
            throw new IllegalStateException("Failed to send OTP email");
        }
    }

    private String buildRegistrationOtpEmail(String name, long otp) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "    <meta charset=\"UTF-8\">"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "</head>"
                + "<body style=\"margin:0;padding:0;font-family:Helvetica,Arial,sans-serif;background-color:#f4f4f4;\">"
                + "    <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#f4f4f4;\">"
                + "        <tr>"
                + "            <td align=\"center\" style=\"padding:20px 0;\">"
                + "                <table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#ffffff;\">"
                + "                    <!-- Header -->"
                + "                    <tr>"
                + "                        <td style=\"background-color:#0b0c0c;padding:30px;text-align:center;\">"
                + "                            <h1 style=\"margin:0;color:#ffffff;font-size:28px;font-weight:700;\">Mã xác thực OTP</h1>"
                + "                        </td>"
                + "                    </tr>"
                + "                    <!-- Content -->"
                + "                    <tr>"
                + "                        <td style=\"padding:40px 30px;\">"
                + "                            <p style=\"margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c;\">Chào <strong>"
                + name + "</strong>,</p>"
                + "                            <p style=\"margin:0 0 30px 0;font-size:16px;line-height:24px;color:#0b0c0c;\">Cảm ơn bạn đã đăng ký tài khoản tại Nike Store. Dưới đây là mã OTP để hoàn tất đăng ký:</p>"
                + "                            <!-- OTP Box -->"
                + "                            <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">"
                + "                                <tr>"
                + "                                    <td align=\"center\" style=\"padding:20px 0;\">"
                + "                                        <div style=\"background-color:#f4f4f4;border-radius:8px;padding:20px;display:inline-block;\">"
                + "                                            <span style=\"font-size:36px;font-weight:bold;color:#1D70B8;letter-spacing:3px;\">"
                + otp + "</span>"
                + "                                        </div>"
                + "                                    </td>"
                + "                                </tr>"
                + "                            </table>"
                + "                            <p style=\"margin:30px 0 20px 0;font-size:16px;line-height:24px;color:#0b0c0c;\"><strong>Mã OTP có hiệu lực trong 5 phút.</strong></p>"
                + "                            <p style=\"margin:0 0 20px 0;font-size:14px;line-height:22px;color:#cc0000;\">⚠️ Tuyệt đối không chia sẻ mã này cho bất kỳ ai!</p>"
                + "                            <p style=\"margin:0 0 30px 0;font-size:14px;line-height:22px;color:#666666;\">Nếu bạn không thực hiện đăng ký này, vui lòng bỏ qua email.</p>"
                + "                            <p style=\"margin:30px 0 0 0;font-size:16px;line-height:24px;color:#0b0c0c;\">Trân trọng,<br><strong>Nike Store Team</strong></p>"
                + "                        </td>"
                + "                    </tr>"
                + "                    <!-- Footer -->"
                + "                    <tr>"
                + "                        <td style=\"background-color:#f8f8f8;padding:20px 30px;text-align:center;border-top:1px solid #e0e0e0;\">"
                + "                            <p style=\"margin:0;font-size:12px;color:#999999;\">© 2025 Nike Store. All rights reserved.</p>"
                + "                        </td>"
                + "                    </tr>"
                + "                </table>"
                + "            </td>"
                + "        </tr>"
                + "    </table>"
                + "</body>"
                + "</html>";
    }

    @Async
    public void sendPasswordResetOtp(String to, String userName, long otp) {
        String emailContent = buildPasswordResetOtpEmail(userName, otp);
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(emailContent, true);
            helper.setTo(to);
            helper.setSubject("Mã OTP đặt lại mật khẩu - Nike Store");
            helper.setFrom("no-reply@nikestore.local");
            mailSender.send(mimeMessage);
            LOGGER.info("✅ Password reset OTP email sent successfully to: {}", to);
        } catch (MessagingException e) {
            LOGGER.error("❌ Failed to send password reset OTP email to: {}", to, e);
            throw new IllegalStateException("Failed to send password reset OTP email");
        }
    }

    private String buildPasswordResetOtpEmail(String name, long otp) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "    <meta charset=\"UTF-8\">"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "</head>"
                + "<body style=\"margin:0;padding:0;font-family:Helvetica,Arial,sans-serif;background-color:#f4f4f4;\">"
                + "    <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#f4f4f4;\">"
                + "        <tr>"
                + "            <td align=\"center\" style=\"padding:20px 0;\">"
                + "                <table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:#ffffff;\">"
                + "                    <!-- Header -->"
                + "                    <tr>"
                + "                        <td style=\"background-color:#dc2626;padding:30px;text-align:center;\">"
                + "                            <h1 style=\"margin:0;color:#ffffff;font-size:28px;font-weight:700;\">🔐 Đặt Lại Mật Khẩu</h1>"
                + "                        </td>"
                + "                    </tr>"
                + "                    <!-- Content -->"
                + "                    <tr>"
                + "                        <td style=\"padding:40px 30px;\">"
                + "                            <p style=\"margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c;\">Chào <strong>"
                + name + "</strong>,</p>"
                + "                            <p style=\"margin:0 0 30px 0;font-size:16px;line-height:24px;color:#0b0c0c;\">Bạn đã yêu cầu đặt lại mật khẩu tài khoản Nike Store. Dưới đây là mã OTP để xác thực:</p>"
                + "                            <!-- OTP Box -->"
                + "                            <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">"
                + "                                <tr>"
                + "                                    <td align=\"center\" style=\"padding:20px 0;\">"
                + "                                        <div style=\"background-color:#fef2f2;border:2px solid #dc2626;border-radius:8px;padding:20px;display:inline-block;\">"
                + "                                            <span style=\"font-size:36px;font-weight:bold;color:#dc2626;letter-spacing:3px;\">"
                + otp + "</span>"
                + "                                        </div>"
                + "                                    </td>"
                + "                                </tr>"
                + "                            </table>"
                + "                            <p style=\"margin:30px 0 20px 0;font-size:16px;line-height:24px;color:#0b0c0c;\"><strong>Mã OTP có hiệu lực trong 5 phút.</strong></p>"
                + "                            <p style=\"margin:0 0 20px 0;font-size:14px;line-height:22px;color:#cc0000;\">⚠️ Tuyệt đối không chia sẻ mã này cho bất kỳ ai!</p>"
                + "                            <p style=\"margin:0 0 30px 0;font-size:14px;line-height:22px;color:#666666;\">Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này và đảm bảo tài khoản của bạn an toàn.</p>"
                + "                            <p style=\"margin:30px 0 0 0;font-size:16px;line-height:24px;color:#0b0c0c;\">Trân trọng,<br><strong>Nike Store Team</strong></p>"
                + "                        </td>"
                + "                    </tr>"
                + "                    <!-- Footer -->"
                + "                    <tr>"
                + "                        <td style=\"background-color:#f8f8f8;padding:20px 30px;text-align:center;border-top:1px solid #e0e0e0;\">"
                + "                            <p style=\"margin:0;font-size:12px;color:#999999;\">© 2025 Nike Store. All rights reserved.</p>"
                + "                        </td>"
                + "                    </tr>"
                + "                </table>"
                + "            </td>"
                + "        </tr>"
                + "    </table>"
                + "</body>"
                + "</html>";
    }

}
