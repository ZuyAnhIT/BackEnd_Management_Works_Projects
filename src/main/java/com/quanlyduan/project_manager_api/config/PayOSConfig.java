package com.quanlyduan.project_manager_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import vn.payos.PayOS;

/**
 * Lớp cấu hình khởi tạo đối tượng PayOS để giao tiếp với cổng thanh toán.
 */
@Configuration
public class PayOSConfig {

    private final String clientId;
    private final String apiKey;
    private final String checksumKey;

    // Khởi tạo thủ công và tiêm các giá trị cấu hình từ application.properties
    public PayOSConfig(
            @Value("${payos.client-id}") String clientId,
            @Value("${payos.api-key}") String apiKey,
            @Value("${payos.checksum-key}") String checksumKey) {
        this.clientId = clientId;
        this.apiKey = apiKey;
        this.checksumKey = checksumKey;
    }

    /**
     * Cấu hình Bean PayOS.
     * Đối tượng này được quản lý như một Singleton để tái sử dụng trong các Service.
     */
    @Bean
    public PayOS payOS() {
        return new PayOS(clientId, apiKey, checksumKey);
    }
}