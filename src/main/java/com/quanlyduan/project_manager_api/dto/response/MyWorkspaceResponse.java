// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/MyWorkspaceResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO phản hồi thông tin tóm tắt về một Không gian làm việc (Workspace) mà người dùng đang tham gia.
 * Được sử dụng chủ yếu trong API Dashboard (/api/dashboard/workspaces) để hiển thị danh sách nhanh.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyWorkspaceResponse {

    // ========================================================================
    // 1. THÔNG TIN KHÔNG GIAN LÀM VIỆC (WORKSPACE INFO)
    // ========================================================================

    // ID định danh của Workspace
    private Integer workspaceId;

    // Tên hiển thị của Workspace
    private String workspaceName;

    // Mã định danh (ví dụ: "DEV-TEAM")
    private String workspaceCode;

    // Mô tả ngắn gọn
    private String workspaceDescription;

    // Đường dẫn ảnh bìa (Cover Image)
    private String workspaceCoverImage;

    // Mã màu đại diện (Hex code)
    private String workspaceColor;

    // Trạng thái của Workspace (ACTIVE, ARCHIVED...)
    private String workspaceStatus;

    // ========================================================================
    // 2. THÔNG TIN CÔNG TY CHỦ QUẢN (PARENT COMPANY INFO)
    // ========================================================================

    // ID của Công ty chứa Workspace này
    private Integer companyId;

    // Tên của Công ty
    private String companyName;

    // Logo của Công ty
    private String companyLogoUrl;

    // ========================================================================
    // 3. THÔNG TIN TƯ CÁCH THÀNH VIÊN (MEMBERSHIP INFO)
    // ========================================================================

    // Mã vai trò của người dùng trong Workspace này (ví dụ: "WORKSPACE_ADMIN").
    // Dùng để phân quyền trên giao diện (Frontend).
    private String roleCode;

    // Tên hiển thị của vai trò (ví dụ: "Quản trị viên").
    private String roleName;

    // Trạng thái thành viên của người dùng trong Workspace này (ACTIVE, REMOVED...).
    private MemberStatus membershipStatus;

    // Thời điểm người dùng tham gia Workspace.
    private LocalDateTime joinedAt;
}