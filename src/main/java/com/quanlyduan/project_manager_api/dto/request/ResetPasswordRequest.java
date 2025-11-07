// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/ResetPasswordRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Token must not be blank") // Đã dịch
    private String token;

    @NotBlank(message = "New password must not be blank") // Đã dịch
    @Size(min = 6, message = "New password must be at least 6 characters long") // Đã dịch
    private String newPassword;
}