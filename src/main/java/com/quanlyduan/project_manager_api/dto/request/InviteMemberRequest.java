package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu khi thực hiện mời một thành viên mới tham gia vào Công ty (hoặc Dự án).
 * Dữ liệu này sẽ được dùng để tạo thiếp mời (Invitation) gửi qua Email.
 */
@Data
public class InviteMemberRequest {

    // ==========================================
    // REQUEST DATA (Thông tin lời mời)
    // ==========================================

    /**
     * Địa chỉ Email của người được mời.
     * Bắt buộc phải nhập và tuân thủ đúng định dạng chuẩn (VD: user@example.com).
     */
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * Mã vai trò (Role Code) dự kiến sẽ phân quyền cho người dùng sau khi họ chấp nhận lời mời.
     * Ví dụ: "COMPANY_ADMIN", "COMPANY_MEMBER", "PROJECT_MANAGER".
     * Bắt buộc phải cung cấp.
     */
    @NotBlank(message = "Role code must not be blank")
    private String roleCode;

}