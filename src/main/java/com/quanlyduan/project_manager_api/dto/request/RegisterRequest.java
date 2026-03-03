package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu đăng ký tài khoản mới trực tiếp từ người dùng (Public Registration).
 */
@Data
public class RegisterRequest {

    // ==========================================
    // REQUEST DATA (Thông tin đăng ký)
    // ==========================================

    /**
     * Họ và tên đầy đủ của người dùng.
     * Bắt buộc phải nhập, không được để trống.
     */
    @NotBlank(message = "Full name must not be blank")
    private String fullName;

    /**
     * Địa chỉ Email sử dụng để đăng ký và đăng nhập sau này.
     * Bắt buộc phải nhập và tuân thủ đúng định dạng (VD: user@example.com).
     */
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * Mật khẩu đăng nhập cho tài khoản mới.
     * Bắt buộc phải nhập và có độ dài tối thiểu là 6 ký tự để đảm bảo an toàn cơ bản.
     */
    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, message = "Password must contain at least 6 characters")
    private String password;

}