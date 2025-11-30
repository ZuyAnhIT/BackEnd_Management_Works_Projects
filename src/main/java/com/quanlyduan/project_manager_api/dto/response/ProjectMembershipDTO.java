// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/ProjectMembershipDTO.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con (Nested DTO) chứa thông tin về tư cách thành viên của người dùng trong một Dự án cụ thể.
 * Thường được sử dụng trong danh sách "My Projects" hoặc thông tin Profile mở rộng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMembershipDTO {

    // ========================================================================
    // 1. THÔNG TIN DỰ ÁN (PROJECT INFO)
    // ========================================================================

    // ID định danh của dự án
    private Integer projectId;

    // Tên hiển thị của dự án
    private String projectName;

    // ========================================================================
    // 2. THÔNG TIN CẤP CHA (HIERARCHY INFO)
    // ========================================================================

    // ID của Không gian làm việc (Workspace) chứa dự án này.
    // Frontend dùng để tạo đường dẫn breadcrumb hoặc link điều hướng.
    private Integer workspaceId; 

    // ========================================================================
    // 3. THÔNG TIN VAI TRÒ (ROLE INFO)
    // ========================================================================

    // Mã vai trò của người dùng trong dự án này.
    // Ví dụ: "PROJECT_ADMIN", "PROJECT_MEMBER", "GUEST_PROJECT".
    private String roleCode; 
}