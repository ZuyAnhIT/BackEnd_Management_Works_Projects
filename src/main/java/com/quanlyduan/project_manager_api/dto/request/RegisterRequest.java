// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/RegisterRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO nhận dữ liệu đăng ký tài khoản mới từ người dùng.
 */
@Data
public class RegisterRequest {

    // Họ và tên đầy đủ (Bắt buộc)
    @NotBlank(message = "Full name must not be blank")
    private String fullName;

    // Email đăng ký (Bắt buộc, đúng định dạng email)
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email;

    // Mật khẩu (Bắt buộc, tối thiểu 6 ký tự để đảm bảo an toàn cơ bản)
    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, message = "Password must contain at least 6 characters")
    private String password;
}