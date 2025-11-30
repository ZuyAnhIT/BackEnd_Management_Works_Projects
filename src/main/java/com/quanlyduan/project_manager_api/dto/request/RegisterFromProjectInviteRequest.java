// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/RegisterFromProjectInviteRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO nhận dữ liệu cho quy trình Đăng ký tài khoản mới từ một Lời mời tham gia DỰ ÁN (Project).
 * Áp dụng cho người dùng bên ngoài (Khách/Đối tác) chưa có tài khoản trong hệ thống.
 */
@Data
public class RegisterFromProjectInviteRequest {

    // Họ và tên hiển thị của người dùng (Bắt buộc)
    @NotBlank(message = "Full name must not be blank")
    private String fullName;

    // Mật khẩu đăng nhập (Bắt buộc, tối thiểu 8 ký tự theo logic gốc)
    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    private String password;

    // Mã Token lời mời dự án (Bắt buộc)
    // Token này giúp xác định người dùng đang chấp nhận lời mời vào Dự án nào.
    @NotBlank(message = "Invitation token must not be blank")
    private String invitationToken; 
}