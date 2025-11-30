// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/GoogleLoginRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO nhận dữ liệu cho yêu cầu đăng nhập bằng tài khoản Google (OAuth2/OIDC).
 * Frontend sẽ gửi token nhận được từ Google (ID Token) lên server để xác thực.
 */
@Data
public class GoogleLoginRequest {
    
    // Chuỗi ID Token (JWT) do Google trả về cho Client.
    // Backend sẽ dùng chuỗi này để xác thực danh tính người dùng với Google Server.
    @NotBlank(message = "Google token must not be blank")
    private String googleToken; 
}