package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Lombok
import lombok.Data;

/**
 * DTO chứa thông tin yêu cầu đổi mật khẩu chủ động của người dùng.
 * Yêu cầu người dùng phải nhập đúng mật khẩu cũ để xác thực.
 */
@Data
public class ChangePasswordRequest {

    // ==========================================
    // REQUEST DATA (Thông tin đổi mật khẩu)
    // ==========================================

    /**
     * Mật khẩu hiện tại.
     * Bắt buộc để xác minh danh tính người dùng trước khi cho phép thực hiện đổi.
     */
    @NotBlank(message = "Old password must not be blank")
    private String oldPassword;

    /**
     * Mật khẩu mới.
     * Bắt buộc phải nhập và đáp ứng độ dài tối thiểu là 6 ký tự.
     */
    @NotBlank(message = "New password must not be blank")
    @Size(min = 6, message = "New password must contain at least 6 characters")
    private String newPassword;

    /**
     * Nhập lại mật khẩu mới để xác nhận.
     * Bắt buộc nhập. Logic kiểm tra khớp với newPassword thường được thực hiện ở tầng Service.
     */
    @NotBlank(message = "Confirm new password must not be blank")
    private String confirmNewPassword;

}