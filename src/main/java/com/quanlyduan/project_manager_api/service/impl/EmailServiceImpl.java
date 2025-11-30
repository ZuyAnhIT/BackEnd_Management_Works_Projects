// File: src/main/java/com/quanlyduan/project_manager_api/service/impl/EmailServiceImpl.java
package com.quanlyduan.project_manager_api.service.impl;

import com.quanlyduan.project_manager_api.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    // Lấy địa chỉ email người gửi từ cấu hình Spring
    @Value("${spring.mail.username}")
    private String fromEmail;

    // ======================================================
    // CONSTRUCTOR (Dependency Injection)
    // ======================================================
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ======================================================
    // LOGIC GỬI EMAIL (SEND EMAIL)
    // ======================================================
    @Override
    // Sử dụng @Async để gửi email bất đồng bộ, tránh làm chậm luồng xử lý API chính
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            // 1. Khởi tạo MimeMessage và Helper
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // Tham số "utf-8" để hỗ trợ ký tự tiếng Việt
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            // 2. Thiết lập thông tin email
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            // Tham số 'true' cho phép nội dung body sử dụng định dạng HTML
            helper.setText(body, true);

            // 3. Thực hiện gửi mail
            mailSender.send(mimeMessage);

            // Sửa thông báo console sang tiếng Anh
            System.out.println("Email sent successfully to: " + to);
        } catch (Exception e) {
            // Sửa thông báo console sang tiếng Anh
            System.err.println("Error while sending email: " + e.getMessage());
        }
    }
}