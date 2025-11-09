// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/InviteWorkspaceMemberRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteWorkspaceMemberRequest {

    @NotBlank(message = "Email must not be blank") // Đã dịch
    @Email(message = "Email is not in a valid format") // Đã dịch
    private String email;

    @NotBlank(message = "Role code must not be blank") // Đã dịch
    private String roleCode; // Đã dịch (Thay cho roleId)
    // Role ID của Workspace (ví dụ: WORKSPACE_MEMBER)
}