package com.foodorder.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Xác nhận đăng ký tài khoản";
        String verificationUrl = "http://localhost:3000/verify?token=" + token;

        String content = "<p>Chào bạn,</p>"
                + "<p>Vui lòng nhấn vào link dưới để xác nhận tài khoản:</p>"
                + "<a href=\"" + verificationUrl + "\">Xác nhận tài khoản</a>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("EMAIL_SEND_FAILED", e);
        }
    }
}
