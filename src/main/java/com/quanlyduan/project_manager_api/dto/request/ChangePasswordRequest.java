// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/ChangePasswordRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Old password must not be blank") // Đã dịch
    private String oldPassword;

    @NotBlank(message = "New password must not be blank") // Đã dịch
    @Size(min = 6, message = "New password must be at least 6 characters long") // Đã dịch
    private String newPassword;

    @NotBlank(message = "Confirm new password must not be blank") // Đã dịch
    private String confirmNewPassword;
}