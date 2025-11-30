// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/WorkspaceMemberResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO phản hồi thông tin chi tiết của một Thành viên trong Không gian làm việc (Workspace).
 * Dùng để hiển thị danh sách thành viên và phân quyền trong phòng ban.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberResponse {

    // ========================================================================
    // 1. THÔNG TIN ĐỊNH DANH & CÁ NHÂN (IDENTITY & USER INFO)
    // ========================================================================

    // ID của bản ghi thành viên Workspace (Dùng cho thao tác quản lý)
    private Integer memberId;

    // ID của tài khoản người dùng (User ID)
    private Integer userId;
    
    // Họ và tên đầy đủ
    private String fullName;

    // Địa chỉ Email
    private String email;

    // Số điện thoại liên hệ
    private String phoneNumber; 
    
    // Đường dẫn ảnh đại diện
    private String avatarUrl;

    // ========================================================================
    // 2. NGỮ CẢNH WORKSPACE (CONTEXT)
    // ========================================================================

    // Tên vai trò của người dùng trong Workspace (ví dụ: "Workspace Admin", "Member")
    private String roleName;

    // Trạng thái hoạt động trong Workspace (ACTIVE, REMOVED)
    private MemberStatus status;

    // Thời điểm người dùng tham gia Workspace
    private LocalDateTime joinedAt;
}