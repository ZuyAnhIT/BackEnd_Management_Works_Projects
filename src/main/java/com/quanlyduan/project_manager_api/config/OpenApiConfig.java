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
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            // 1. Thêm thông tin chung cho API
            .info(new Info()
                .title("Project Manager API")
                .version("v1.0")
                .description("API Documentation for the Project and Task Management System.")
                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
            
            // 2. Thêm yêu cầu bảo mật (nút Authorize) cho tất cả API
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            
            // 3. Định nghĩa Security Scheme (Bearer Token - JWT)
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP) // Loại là HTTP
                        .scheme("bearer")               // Scheme là "bearer"
                        .bearerFormat("JWT")            // Định dạng là JWT
                        .description("Enter your JWT Token to access the API.")
                )
            );
    }
}