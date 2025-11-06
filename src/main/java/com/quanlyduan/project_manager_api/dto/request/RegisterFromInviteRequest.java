// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/RegisterFromInviteRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterFromInviteRequest {
    @NotBlank(message = "Full name must not be blank") // Đã dịch
    private String fullName; // Đã dịch

    @NotBlank(message = "Password must not be blank") // Đã dịch
    @Size(min = 6, message = "Password must be at least 6 characters long") // Đã dịch
    private String password; // Đã dịch

    @NotBlank(message = "Invitation token must not be blank") // Đã dịch
    private String invitationToken;
}