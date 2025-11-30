// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/LogoutRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO nhận dữ liệu cho yêu cầu đăng xuất.
 * Client cần gửi Refresh Token lên để Server thực hiện thu hồi (revoke) hoặc xóa bỏ token đó,
 * ngăn chặn việc sử dụng lại token để lấy Access Token mới.
 */
@Data
public class LogoutRequest {

    // Mã Token làm mới cần bị vô hiệu hóa (Bắt buộc, không được để trống)
    @NotBlank(message = "Refresh token must not be blank")
    private String refreshToken;
}