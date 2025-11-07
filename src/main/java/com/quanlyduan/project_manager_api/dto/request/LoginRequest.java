// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/LoginRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email must not be blank") // Đã dịch
    @Email(message = "Email is not in a valid format") 
    private String email;

    @NotBlank(message = "Password must not be blank") // Đã dịch
    private String password; // Đã dịch
}