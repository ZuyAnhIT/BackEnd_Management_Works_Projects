// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/InviteMemberRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO nhận dữ liệu khi thực hiện mời một thành viên mới vào Công ty.
 */
@Data
public class InviteMemberRequest {

    // Email của người được mời (Bắt buộc & Phải đúng định dạng email)
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email;

    // Mã vai trò muốn gán cho người được mời (Bắt buộc)
    // Ví dụ: "COMPANY_ADMIN", "COMPANY_MEMBER"
    @NotBlank(message = "Role code must not be blank")
    private String roleCode; 
}