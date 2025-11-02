package com.quanlyduan.project_manager_api.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyEmailRequest {
    @NotBlank(message = "Email không được để trống")
    @Email
    private String email;

    @NotBlank(message = "OTP không được để trống")
    private String otp;
}