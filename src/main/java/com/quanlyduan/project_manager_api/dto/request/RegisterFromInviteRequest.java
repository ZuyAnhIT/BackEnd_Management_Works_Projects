// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/RegisterFromInviteRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO nhận dữ liệu cho quy trình Đăng ký tài khoản mới từ một Lời mời (Invitation).
 * Áp dụng cho trường hợp người dùng nhận được email mời vào Công ty nhưng chưa có tài khoản hệ thống.
 */
@Data
public class RegisterFromInviteRequest {

    // Họ và tên hiển thị của người dùng (Bắt buộc)
    @NotBlank(message = "Full name must not be blank") 
    private String fullName; 

    // Mật khẩu đăng nhập (Bắt buộc, tối thiểu 6 ký tự)
    @NotBlank(message = "Password must not be blank") 
    @Size(min = 6, message = "Password must contain at least 6 characters") 
    private String password; 

    // Mã Token lời mời (Bắt buộc)
    // Token này dùng để xác định người dùng đang chấp nhận lời mời của Công ty nào và Email nào.
    @NotBlank(message = "Invitation token must not be blank") 
    private String invitationToken;
}