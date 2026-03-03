package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Lombok
import lombok.Data;

/**
 * DTO nhận yêu cầu luồng "Quên mật khẩu" từ phía người dùng.
 * Yêu cầu người dùng cung cấp email đã đăng ký để hệ thống gửi liên kết chứa Token đặt lại mật khẩu.
 */
@Data
public class ForgotPasswordRequest {

    // ==========================================
    // REQUEST DATA (Thông tin định danh)
    // ==========================================

    /**
     * Địa chỉ Email của người dùng cần khôi phục mật khẩu.
     * Bắt buộc phải nhập và phải tuân thủ đúng định dạng email tiêu chuẩn (VD: user@example.com).
     */
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email;

}