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

    @Value("${google.client-id}")
    private String googleClientId;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // *** THÊM BEAN NÀY ***
    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier() {
        // Bean này sẽ được dùng để xác thực token từ Google
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
            .setAudience(Collections.singletonList(googleClientId))
            .build();
    }
    
    // Các Bean khác (như ModelMapper) có thể được thêm vào đây sau
}