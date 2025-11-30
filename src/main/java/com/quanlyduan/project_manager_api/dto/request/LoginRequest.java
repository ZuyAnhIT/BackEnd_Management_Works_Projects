// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/LoginRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO nhận dữ liệu yêu cầu đăng nhập từ người dùng.
 * Bao gồm email và mật khẩu.
 */
@Data
public class LoginRequest {

    // Email đăng nhập (Bắt buộc, không được để trống và phải đúng định dạng)
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email;

    // Mật khẩu đăng nhập (Bắt buộc, không được để trống)
    @NotBlank(message = "Password must not be blank")
    private String password;
}