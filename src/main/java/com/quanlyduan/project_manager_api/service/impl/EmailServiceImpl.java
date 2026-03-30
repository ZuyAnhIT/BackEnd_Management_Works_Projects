package com.quanlyduan.project_manager_api.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.quanlyduan.project_manager_api.service.EmailService;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    // Khai bao cac hang so de loai bo hardcode
    public static final String ENCODING_UTF8 = "utf-8";
    public static final String LOG_SUCCESS_PREFIX = "Email sent successfully to: ";
    public static final String LOG_ERROR_PREFIX = "Error while sending email: ";

    // Khai bao cac bien phu thuoc va gia tri cau hinh
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Constructor khoi tao thu cong thay the cho @RequiredArgsConstructor
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // --- CAC HAM PUBLIC THUC THI NGHIEP VU CHINH ---

    @Override
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            // Khoi tao thong diệp email va cong cu ho tro voi chuan ma hoa UTF-8
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, ENCODING_UTF8);

            // Thiet lap cac thong tin co ban cua email
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            
            // Truyen noi dung email, tham so true cho phep hien thi dinh dang HTML
            helper.setText(body, true);

            // Thuc hien gui email thong qua may chu SMTP
            mailSender.send(mimeMessage);

            // Ghi log xac nhan hoan tat gui email tren production
            System.out.println(LOG_SUCCESS_PREFIX + to);
            
        } catch (Exception e) {
            // Ghi log loi neu qua trinh gui email that bai de de dang trace loi
            System.err.println(LOG_ERROR_PREFIX + e.getMessage());
        }
    }
}