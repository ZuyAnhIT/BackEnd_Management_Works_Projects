package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;


    // LOGIC FORM EMAIL
    @Override
    @Async // (Optional) Gửi email bất đồng bộ để không block luồng chính
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = hỗ trợ HTML

            mailSender.send(mimeMessage);
            System.out.println("Email đã gửi thành công đến: " + to);
        } catch (Exception e) {
            // (Nên log lỗi này)
            System.err.println("Lỗi khi gửi email: " + e.getMessage());
        }
    }
}