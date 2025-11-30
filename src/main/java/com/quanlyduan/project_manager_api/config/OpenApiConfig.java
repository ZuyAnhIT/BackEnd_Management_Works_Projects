// File: src/main/java/com/quanlyduan/project_manager_api/config/OpenApiConfig.java
package com.quanlyduan.project_manager_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Định nghĩa tên định danh cho scheme bảo mật
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            // 1. Cấu hình thông tin chung về API (Tiêu đề, Phiên bản, Mô tả)
            .info(new Info()
                .title("Project Manager API")
                .version("v1.0")
                .description("API Documentation for the Project and Task Management System.") // Mô tả hiển thị trên Swagger UI
                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
            
            // 2. Áp dụng yêu cầu bảo mật (Hiện nút Authorize - Ổ khóa) cho toàn bộ các endpoint
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            
            // 3. Định nghĩa chi tiết về cơ chế bảo mật (Sử dụng JWT Bearer Token)
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP) // Loại xác thực là HTTP
                        .scheme("bearer")               // Sử dụng cơ chế Bearer Token
                        .bearerFormat("JWT")            // Định dạng token là JWT
                        .description("Enter your JWT Token to access the API.") // Hướng dẫn người dùng
                )
            );
    }
}