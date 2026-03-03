package com.quanlyduan.project_manager_api.dto.request;

// Validation
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Lombok
import lombok.Data;

/**
 * DTO nhận dữ liệu cho quy trình Đăng ký tài khoản mới thông qua Lời mời (Invitation).
 * Áp dụng trong trường hợp người dùng nhận được email mời tham gia hệ thống nhưng chưa có tài khoản.
 */
@Data
public class RegisterFromInviteRequest {

    // ==========================================
    // REQUEST DATA (Thông tin đăng ký qua lời mời)
    // ==========================================

    /**
     * Họ và tên hiển thị của người dùng mới.
     * Bắt buộc phải có, không được để trống.
     */
    @NotBlank(message = "Full name must not be blank") 
    private String fullName; 

    /**
     * Mật khẩu đăng nhập cho tài khoản mới.
     * Bắt buộc phải có và phải đạt độ dài tối thiểu là 6 ký tự.
     */
    @NotBlank(message = "Password must not be blank") 
    @Size(min = 6, message = "Password must contain at least 6 characters") 
    private String password; 

    /**
     * Mã Token lời mời (Invitation Token) trích xuất từ đường link trong Email.
     * Token này chứa thông tin xác định người dùng đang chấp nhận lời mời của Công ty nào và với Email nào.
     * Bắt buộc phải cung cấp để hệ thống giải mã và đối chiếu.
     */
    @NotBlank(message = "Invitation token must not be blank") 
    private String invitationToken;

}