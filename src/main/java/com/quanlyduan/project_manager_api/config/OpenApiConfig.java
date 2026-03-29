package com.quanlyduan.project_manager_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Lớp cấu hình giao diện tài liệu API (Swagger/OpenAPI).
 * Thiết lập thông tin chung và yêu cầu cơ chế xác thực JWT cho toàn bộ hệ thống API.
 */
@Configuration
public class OpenApiConfig {

    // Khai báo các hằng số thông tin dự án và cấu hình bảo mật
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String API_TITLE = "Project Manager API";
    private static final String API_VERSION = "v1.0";
    private static final String API_DESCRIPTION = "API Documentation for the Project and Task Management System.";
    private static final String LICENSE_NAME = "Apache 2.0";
    private static final String LICENSE_URL = "http://springdoc.org";
    private static final String SCHEME_TYPE = "bearer";
    private static final String BEARER_FORMAT = "JWT";
    private static final String SECURITY_DESCRIPTION = "Enter your JWT Token to access the API.";

    /**
     * Khởi tạo Bean cấu hình OpenAPI.
     * Tích hợp thông tin dự án và yêu cầu xác thực bảo mật.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(getApiInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(getSecurityComponents());
    }

    /**
     * Thiết lập thông tin chung hiển thị trên giao diện tài liệu API.
     */
    private Info getApiInfo() {
        return new Info()
                .title(API_TITLE)
                .version(API_VERSION)
                .description(API_DESCRIPTION)
                .license(new License().name(LICENSE_NAME).url(LICENSE_URL));
    }

    /**
     * Thiết lập cấu hình bảo mật JWT (hiển thị nút Authorize / Ổ khóa) cho Swagger.
     */
    private Components getSecurityComponents() {
        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme(SCHEME_TYPE)
                                .bearerFormat(BEARER_FORMAT)
                                .description(SECURITY_DESCRIPTION)
                );
    }
}