package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu cho quy trình Đăng ký tài khoản mới từ Lời mời tham gia Dự án (Project Invitation).
 * Áp dụng đặc thù cho người dùng bên ngoài (Khách/Đối tác/Freelancer) chưa từng có tài khoản trên hệ thống.
 */
@Data
public class RegisterFromProjectInviteRequest {

    // ==========================================
    // REQUEST DATA (Thông tin đăng ký Guest)
    // ==========================================

    /**
     * Họ và tên hiển thị của người dùng (Guest).
     * Bắt buộc phải có, không được để trống.
     */
    @NotBlank(message = "Full name must not be blank")
    private String fullName;

    /**
     * Mật khẩu đăng nhập cho tài khoản Guest mới.
     * Bắt buộc phải có và phải đạt độ dài tối thiểu là 8 ký tự.
     */
    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    private String password;

    /**
     * Mã Token lời mời dự án (Project Invitation Token).
     * Token này chứa thông tin giải mã để xác định người dùng đang chấp nhận lời mời vào Dự án nào và với vai trò gì.
     * Bắt buộc phải cung cấp.
     */
    @NotBlank(message = "Invitation token must not be blank")
    private String invitationToken; 

}