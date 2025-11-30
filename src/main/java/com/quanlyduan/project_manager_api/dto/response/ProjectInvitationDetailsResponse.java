// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/ProjectInvitationDetailsResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO phản hồi chi tiết của một Lời mời tham gia Dự án.
 * Được sử dụng khi người dùng click vào link trong email để hiển thị thông tin chào mừng
 * và quyết định luồng tiếp theo (Đăng ký hay Đăng nhập).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInvitationDetailsResponse {

    // Email của người được mời (dùng để hiển thị và pre-fill form)
    private String email;

    // Tên dự án mà họ được mời vào (để hiển thị ngữ cảnh: "Bạn được mời vào dự án X")
    private String projectName;

    // Tên vai trò dự kiến sẽ được gán (ví dụ: "Guest", "Developer")
    private String roleName;

    // Cờ quan trọng để Frontend điều hướng:
    // - true: Email đã tồn tại trong hệ thống -> Hiển thị form Đăng nhập.
    // - false: Email chưa tồn tại -> Hiển thị form Đăng ký tài khoản mới.
    private boolean accountExists;

    // Thời gian hết hạn của token lời mời.
    // Frontend có thể dùng để hiển thị đếm ngược hoặc thông báo lỗi nếu đã hết hạn.
    private LocalDateTime expiresAt;
}