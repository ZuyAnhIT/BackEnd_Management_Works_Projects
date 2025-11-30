// File: src/main/java/com/quanlyduan/project_manager_api/config/ApplicationConfig.java
package com.quanlyduan.project_manager_api.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@Configuration
public class ApplicationConfig {

    // Lấy giá trị Google Client ID từ file cấu hình (application.properties)
    @Value("${google.client-id}")
    private String googleClientId;

    /**
     * Khởi tạo Bean PasswordEncoder.
     * Sử dụng thuật toán BCrypt để mã hóa và kiểm tra mật khẩu người dùng.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Khởi tạo Bean GoogleIdTokenVerifier.
     * Bean này chịu trách nhiệm xác thực tính hợp lệ của Google ID Token gửi từ Client.
     */
    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        // Xây dựng verifier sử dụng NetHttpTransport và GsonFactory chuẩn của Google
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
            // Thiết lập danh sách Audience hợp lệ (chính là Client ID của ứng dụng này)
            // Điều này đảm bảo token được cấp phát cho đúng ứng dụng của chúng ta
            .setAudience(Collections.singletonList(googleClientId))
            .build();
    }

    // (Ghi chú: Các Bean cấu hình khác như ModelMapper có thể được thêm vào đây trong tương lai)
}