// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/RegisterRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Họ và tên không được để trống") // Đã dịch
    private String fullName; // Đã dịch

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống") // Đã dịch
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự") // Đã dịch
    private String password; // Đã dịch
}