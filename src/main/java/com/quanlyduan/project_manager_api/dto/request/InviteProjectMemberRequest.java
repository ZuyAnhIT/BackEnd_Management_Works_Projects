// File: src/main/java/com/quanlyduan.project_manager_api/dto/request/InviteProjectMemberRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteProjectMemberRequest {
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email; 

    @NotBlank(message = "Mã vai trò (roleCode) không được để trống")
    private String roleCode; // Ví dụ: PROJECT_MEMBER, GUEST_PROJECT
}