// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/ForgotPasswordRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO nhận yêu cầu quên mật khẩu.
 * Người dùng chỉ cần cung cấp email để hệ thống gửi link đặt lại mật khẩu.
 */
@Data
public class ForgotPasswordRequest {

    // Email của người dùng (Bắt buộc, phải đúng định dạng email)
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email;
}