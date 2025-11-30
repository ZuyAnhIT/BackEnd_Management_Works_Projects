// File: src/main/java/com/quanlyduan/project_manager_api/config/WebConfig.java
package com.quanlyduan.project_manager_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Lấy đường dẫn thư mục upload từ file cấu hình (application.properties)
    // Mặc định là "uploads" nếu không tìm thấy cấu hình
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Cấu hình Resource Handler để phục vụ các file tĩnh (ảnh, tài liệu) đã upload.
     * Giúp truy cập file qua URL dạng: http://domain.com/uploads/ten-file.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Xác định đường dẫn vật lý tuyệt đối của thư mục upload trên server
        Path uploadPath = Paths.get(uploadDir);
        String uploadAbsolutePath = uploadPath.toFile().getAbsolutePath();

        // 2. Đăng ký handler:
        // - Khi request bắt đầu bằng "/uploads/**"
        // - Spring sẽ tìm file tương ứng trong thư mục vật lý "file:..."
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadAbsolutePath + "/");
    }
}