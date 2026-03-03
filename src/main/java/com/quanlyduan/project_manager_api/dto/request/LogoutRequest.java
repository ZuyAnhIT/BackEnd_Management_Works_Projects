package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.NotBlank;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu cho yêu cầu đăng xuất (Logout).
 * Client cần gửi Refresh Token lên để Server thực hiện thu hồi (Revoke) hoặc xóa bỏ token đó,
 * nhằm ngăn chặn việc sử dụng lại token này để lấy Access Token mới trong tương lai.
 */
@Data
public class LogoutRequest {

    // ==========================================
    // REQUEST DATA (Thông tin Token)
    // ==========================================

    /**
     * Chuỗi Refresh Token cần bị vô hiệu hóa.
     * Bắt buộc phải có, không được để trống.
     */
    @NotBlank(message = "Refresh token must not be blank")
    private String refreshToken;

}