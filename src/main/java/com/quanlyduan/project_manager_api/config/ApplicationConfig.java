package com.quanlyduan.project_manager_api.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

/**
 * Lớp cấu hình các Bean chung cho toàn bộ ứng dụng.
 */
@Configuration
public class ApplicationConfig {

    private final String googleClientId;

    // Khởi tạo thủ công và inject giá trị từ application.properties
    public ApplicationConfig(@Value("${google.client-id}") String googleClientId) {
        this.googleClientId = googleClientId;
    }

    /**
     * Cấu hình Bean mã hóa mật khẩu.
     * Sử dụng thuật toán BCrypt mặc định của Spring Security.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cấu hình Bean xác thực Google ID Token.
     * Xác minh tính hợp lệ và đảm bảo token được cấp phát đúng cho ứng dụng (dựa vào Client ID).
     */
    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }
}