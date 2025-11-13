// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/ResetPasswordRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Token không được để trống") // Đã dịch
    private String token;

    @NotBlank(message = "Mật khẩu mới không được để trống") // Đã dịch
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự") // Đã dịch
    private String newPassword;
}