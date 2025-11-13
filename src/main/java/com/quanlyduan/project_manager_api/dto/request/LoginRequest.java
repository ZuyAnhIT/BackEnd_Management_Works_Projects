// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/LoginRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email không được để trống") // Đã dịch
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống") // Đã dịch
    private String password; // Đã dịch
}