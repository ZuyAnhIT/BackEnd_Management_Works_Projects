package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu khi thực hiện mời một thành viên tham gia vào Dự án (Project).
 * Áp dụng chung cho cả việc phân công thành viên nội bộ công ty và mời khách (Guest) từ bên ngoài.
 */
@Data
public class InviteProjectMemberRequest {

    // ==========================================
    // REQUEST DATA (Thông tin lời mời dự án)
    // ==========================================

    /**
     * Địa chỉ Email của người được mời vào dự án.
     * Bắt buộc phải nhập và tuân thủ đúng định dạng chuẩn (VD: user@example.com).
     */
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email; 

    /**
     * Mã vai trò cấp Dự án dự kiến sẽ phân quyền cho người dùng này.
     * Ví dụ: "PROJECT_ADMIN", "PROJECT_MEMBER", "GUEST_PROJECT".
     * Bắt buộc phải cung cấp.
     */
    @NotBlank(message = "Role code must not be blank")
    private String roleCode; 

}