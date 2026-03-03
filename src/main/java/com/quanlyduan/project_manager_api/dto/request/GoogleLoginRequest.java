package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.NotBlank;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu cho yêu cầu đăng nhập bằng tài khoản Google (OAuth2/OIDC).
 * Frontend sẽ gửi Token nhận được từ Google (ID Token) lên Server để xác thực.
 */
@Data
public class GoogleLoginRequest {

    // ==========================================
    // REQUEST DATA (Thông tin Token)
    // ==========================================

    /**
     * Chuỗi ID Token (định dạng JWT) do Google trả về cho Client.
     * Backend sẽ sử dụng chuỗi này để xác minh (verify) danh tính người dùng với Google Server.
     * Bắt buộc phải có, không được để trống.
     */
    @NotBlank(message = "Google token must not be blank")
    private String googleToken; 

}