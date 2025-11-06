// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/VerifyEmailRequest.java
package com.quanlyduan.project_manager_api.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyEmailRequest {
    @NotBlank(message = "Email must not be blank") // Đã dịch
    @Email
    private String email;

    @NotBlank(message = "OTP must not be blank") // Đã dịch
    private String otp;
}