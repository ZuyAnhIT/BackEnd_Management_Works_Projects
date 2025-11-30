// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/InviteProjectMemberRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO nhận dữ liệu khi thực hiện mời một thành viên vào Dự án (Project).
 * Áp dụng cho cả việc mời thành viên nội bộ công ty và khách bên ngoài.
 */
@Data
public class InviteProjectMemberRequest {
    
    // Email của người được mời (Bắt buộc & Phải đúng định dạng email)
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email; 

    // Mã vai trò cấp Dự án muốn gán cho người được mời (Bắt buộc)
    // Ví dụ: "PROJECT_ADMIN", "PROJECT_MEMBER", "GUEST_PROJECT"
    @NotBlank(message = "Role code must not be blank")
    private String roleCode; 
}