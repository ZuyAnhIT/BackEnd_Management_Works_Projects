// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/InviteWorkspaceMemberRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteWorkspaceMemberRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mã vai trò không được để trống") // Đã dịch
    private String roleCode; // Đã dịch (Thay cho roleId)
    // Role ID của Workspace (ví dụ: WORKSPACE_MEMBER)
}