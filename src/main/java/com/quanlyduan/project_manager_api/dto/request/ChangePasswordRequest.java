// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/ChangePasswordRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO chứa thông tin yêu cầu đổi mật khẩu của người dùng.
 * Yêu cầu người dùng phải nhập đúng mật khẩu cũ để xác thực.
 */
@Data
public class ChangePasswordRequest {

    // Mật khẩu hiện tại (bắt buộc để xác minh danh tính)
    @NotBlank(message = "Old password must not be blank")
    private String oldPassword;

    // Mật khẩu mới (bắt buộc, tối thiểu 6 ký tự)
    @NotBlank(message = "New password must not be blank")
    @Size(min = 6, message = "New password must contain at least 6 characters")
    private String newPassword;

    // Xác nhận mật khẩu mới (bắt buộc, phải khớp với newPassword - logic so khớp thường nằm ở Service)
    @NotBlank(message = "Confirm new password must not be blank")
    private String confirmNewPassword;
}