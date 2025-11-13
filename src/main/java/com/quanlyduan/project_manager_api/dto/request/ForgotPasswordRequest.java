// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/ForgotPasswordRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "Email không được để trống") // Đã dịch
    @Email(message = "Email không đúng định dạng") // Đã dịch
    private String email;
}