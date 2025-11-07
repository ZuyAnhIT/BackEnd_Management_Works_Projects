// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/ForgotPasswordRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "Email must not be blank") // Đã dịch
    @Email(message = "Email is not in a valid format") // Đã dịch
    private String email;
}