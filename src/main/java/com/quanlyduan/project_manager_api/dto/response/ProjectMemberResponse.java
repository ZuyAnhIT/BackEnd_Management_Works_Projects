// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/ProjectMemberResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO phản hồi thông tin chi tiết của một Thành viên trong Dự án (Project).
 * Dùng để hiển thị danh sách thành viên, phân quyền hoặc tìm kiếm trong dự án.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberResponse {

    // ========================================================================
    // 1. THÔNG TIN ĐỊNH DANH (IDENTITY)
    // ========================================================================

    // ID định danh của bản ghi trong bảng 'project_members'.
    // Dùng để thực hiện các hành động như: Xóa khỏi dự án, Đổi vai trò.
    private Integer memberId;

    // ID của tài khoản người dùng (User ID).
    // Dùng để liên kết đến trang cá nhân hoặc thực hiện các tác vụ liên quan đến User.
    private Integer userId;

    // ========================================================================
    // 2. THÔNG TIN CÁ NHÂN (PERSONAL INFO)
    // ========================================================================

    // Họ và tên hiển thị.
    private String fullName;

    // Địa chỉ Email liên hệ.
    private String email;

    // Số điện thoại liên hệ.
    private String phoneNumber;

    // Đường dẫn ảnh đại diện (Avatar).
    private String avatarUrl;

    // ========================================================================
    // 3. THÔNG TIN TRONG DỰ ÁN (PROJECT CONTEXT)
    // ========================================================================

    // Tên vai trò của thành viên trong dự án này (ví dụ: "Project Admin", "Developer").
    private String roleName;

    // Thời điểm thành viên được thêm vào dự án.
    private LocalDateTime joinedAt;

    // Trạng thái hoạt động của thành viên trong dự án (ACTIVE, REMOVED...).
    private MemberStatus status;
}