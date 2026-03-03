package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu khi thực hiện mời một thành viên vào Không gian làm việc (Workspace).
 * Lưu ý ràng buộc nghiệp vụ: Người được mời bắt buộc phải là thành viên của Công ty quản lý Workspace này trước đó.
 */
@Data
public class InviteWorkspaceMemberRequest {

    // ==========================================
    // REQUEST DATA (Thông tin lời mời Workspace)
    // ==========================================

    /**
     * Địa chỉ Email của người được mời vào Workspace.
     * Bắt buộc phải nhập và tuân thủ đúng định dạng email tiêu chuẩn (VD: user@example.com).
     */
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * Mã vai trò cấp Workspace dự kiến sẽ phân quyền cho người dùng.
     * Ví dụ: "WORKSPACE_ADMIN", "WORKSPACE_MEMBER".
     * Bắt buộc phải cung cấp.
     */
    @NotBlank(message = "Role code must not be blank")
    private String roleCode;

}