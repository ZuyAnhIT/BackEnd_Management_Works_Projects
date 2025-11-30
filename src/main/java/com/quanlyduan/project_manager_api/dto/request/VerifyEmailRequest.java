// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/VerifyEmailRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO nhận dữ liệu cho yêu cầu xác thực tài khoản (Email Verification).
 * Người dùng nhập mã OTP (One-Time Password) đã nhận được qua email để kích hoạt tài khoản.
 */
@Data
public class VerifyEmailRequest {

    // Email cần xác thực (Bắt buộc, đúng định dạng)
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email;

    // Mã OTP người dùng nhập vào (Bắt buộc)
    @NotBlank(message = "OTP must not be blank")
    private String otp;
}