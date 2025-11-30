// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/InvitationDetailsResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO phản hồi chi tiết của một lời mời (khi người dùng nhấp vào link mời trong email).
 * API này thường được gọi công khai (Public) để Frontend quyết định giao diện tiếp theo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationDetailsResponse {

    // Email của người được mời.
    // Frontend dùng trường này để tự động điền vào ô Email (và có thể khóa ô này lại).
    private String email;

    // Tên công ty đã gửi lời mời.
    // Dùng để hiển thị thông báo chào mừng: "Công ty [companyName] đã mời bạn tham gia..."
    private String companyName;

    // Cờ kiểm tra tài khoản tồn tại.
    // - true: Email này đã có tài khoản -> Frontend chuyển hướng sang trang Đăng nhập.
    // - false: Email này chưa có tài khoản -> Frontend chuyển hướng sang trang Đăng ký.
    private boolean accountExists;
}