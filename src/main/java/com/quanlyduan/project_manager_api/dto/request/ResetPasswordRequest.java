// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/ResetPasswordRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO nhận dữ liệu cho yêu cầu đặt lại mật khẩu mới.
 * Được sử dụng sau khi người dùng nhấn vào link "Quên mật khẩu" từ email.
 */
@Data
public class ResetPasswordRequest {

    // Token xác thực quyền đặt lại mật khẩu (được gửi kèm trong link email)
    @NotBlank(message = "Token must not be blank")
    private String token;

    // Mật khẩu mới mà người dùng muốn thiết lập (Bắt buộc, tối thiểu 6 ký tự)
    @NotBlank(message = "New password must not be blank")
    @Size(min = 6, message = "New password must contain at least 6 characters")
    private String newPassword;
}