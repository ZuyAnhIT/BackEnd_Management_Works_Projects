package com.quanlyduan.project_manager_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

/**
 * Cấu hình khởi tạo đối tượng PayOS để giao tiếp với cổng thanh toán.
 * Spring Boot sẽ tự động nạp các key từ file application.properties (đã được link với .env)
 */
@Configuration
public class PayOSConfig {

    // Nạp Client ID từ cấu hình
    @Value("${payos.client-id}")
    private String clientId;

    // Nạp API Key từ cấu hình
    @Value("${payos.api-key}")
    private String apiKey;

    // Nạp Checksum Key từ cấu hình (dùng để verify Webhook)
    @Value("${payos.checksum-key}")
    private String checksumKey;

    /**
     * Khởi tạo Bean PayOS.
     * Đối tượng này sẽ được Spring quản lý như một Singleton và có thể được 
     * @Autowired (hoặc Inject qua Constructor) vào bất kỳ Service nào cần dùng.
     */
    @Bean
    public PayOS payOS() {
        return new PayOS(clientId, apiKey, checksumKey);
    }
}