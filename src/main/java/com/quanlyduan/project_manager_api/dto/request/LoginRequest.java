package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu yêu cầu đăng nhập bằng tài khoản truyền thống từ người dùng.
 * Bao gồm thông tin định danh (Email) và thông tin xác thực (Mật khẩu).
 */
@Data
public class LoginRequest {

    // ==========================================
    // REQUEST DATA (Thông tin đăng nhập)
    // ==========================================

    /**
     * Địa chỉ Email sử dụng để đăng nhập.
     * Bắt buộc phải có, không được để trống và phải tuân thủ đúng định dạng (VD: user@example.com).
     */
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * Mật khẩu đăng nhập.
     * Bắt buộc phải có, không được để trống.
     */
    @NotBlank(message = "Password must not be blank")
    private String password;

}