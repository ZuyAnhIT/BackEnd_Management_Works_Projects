// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/VerifyEmailRequest.java
package com.quanlyduan.project_manager_api.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyEmailRequest {
    @NotBlank(message = "Email không được để trống")// Đã dịch
    @Email
    private String email;

    @NotBlank(message = "OTP không được để trống")// Đã dịch
    private String otp;
}