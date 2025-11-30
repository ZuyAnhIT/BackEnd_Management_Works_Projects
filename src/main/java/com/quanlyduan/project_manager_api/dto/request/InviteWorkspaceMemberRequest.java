// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/InviteWorkspaceMemberRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO nhận dữ liệu khi thực hiện mời thành viên vào Không gian làm việc (Workspace).
 * Lưu ý: Người được mời phải là thành viên của Công ty trước đó.
 */
@Data
public class InviteWorkspaceMemberRequest {

    // Email của người được mời (Bắt buộc, đúng định dạng)
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email;

    // Mã vai trò cấp Workspace muốn gán (Bắt buộc)
    // Ví dụ: "WORKSPACE_ADMIN", "WORKSPACE_MEMBER"
    @NotBlank(message = "Role code must not be blank")
    private String roleCode;
}