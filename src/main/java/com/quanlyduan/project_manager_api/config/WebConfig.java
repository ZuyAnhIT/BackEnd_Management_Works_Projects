package com.quanlyduan.project_manager_api.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình Web MVC để xử lý các tài nguyên tĩnh.
 * Cung cấp quyền truy cập qua HTTP cho các file (ảnh, tài liệu,...) đã được upload lên server.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String RESOURCE_HANDLER_PATTERN = "/uploads/**";
    private static final String RESOURCE_LOCATION_PREFIX = "file:";

    private final String uploadDir;

    // Khởi tạo thủ công và tiêm giá trị cấu hình thư mục upload (mặc định là "uploads")
    public WebConfig(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    /**
     * Đăng ký bộ xử lý tài nguyên tĩnh.
     * Ánh xạ các request HTTP (ví dụ: /uploads/image.png) tới thư mục vật lý chứa file trên server.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadAbsolutePath = getUploadAbsolutePath();

        registry.addResourceHandler(RESOURCE_HANDLER_PATTERN)
                .addResourceLocations(RESOURCE_LOCATION_PREFIX + uploadAbsolutePath + "/");
    }

    /**
     * Lấy đường dẫn vật lý tuyệt đối của thư mục upload.
     */
    private String getUploadAbsolutePath() {
        Path uploadPath = Paths.get(uploadDir);
        return uploadPath.toFile().getAbsolutePath();
    }
}