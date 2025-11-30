// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/LoginResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO phản hồi sau khi người dùng đăng nhập thành công.
 * Chứa các thông tin xác thực cần thiết (Token) để Client lưu trữ và sử dụng cho các request tiếp theo.
 */
@Data
@Builder
@NoArgsConstructor // Bổ sung NoArgsConstructor để đảm bảo tương thích với các thư viện JSON parser
@AllArgsConstructor
public class LoginResponse {

    // Access Token (Token truy cập):
    // - Dùng để xác thực trong Header của mỗi request (Authorization: Bearer <token>).
    // - Có thời hạn ngắn (ví dụ: 30 phút - 1 giờ).
    private String accessToken;

    // Refresh Token (Token làm mới):
    // - Dùng để lấy cấp lại Access Token mới khi cái cũ hết hạn mà không cần đăng nhập lại.
    // - Có thời hạn dài (ví dụ: 7 ngày - 30 ngày).
    // - Cần được lưu trữ an toàn (ví dụ: HttpOnly Cookie hoặc Secure Storage).
    private String refreshToken;

    // Loại Token (Mặc định là "Bearer").
    // Client sẽ ghép chuỗi này với Access Token khi gửi request.
    @Builder.Default
    private String tokenType = "Bearer";
}