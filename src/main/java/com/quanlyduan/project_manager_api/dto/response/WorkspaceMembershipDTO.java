// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/WorkspaceMembershipDTO.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con (Nested DTO) chứa thông tin về tư cách thành viên của người dùng trong một Không gian làm việc (Workspace) cụ thể.
 * Thường được sử dụng trong danh sách "My Workspaces" hoặc Profile người dùng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMembershipDTO {

    // ========================================================================
    // 1. THÔNG TIN KHÔNG GIAN LÀM VIỆC (WORKSPACE INFO)
    // ========================================================================

    // ID định danh của Workspace
    private Integer workspaceId;

    // Tên hiển thị của Workspace
    private String workspaceName;

    // ========================================================================
    // 2. THÔNG TIN CẤP CHA (HIERARCHY INFO)
    // ========================================================================

    // ID của Công ty chứa Workspace này (Context cha)
    private Integer companyId; 

    // ========================================================================
    // 3. THÔNG TIN VAI TRÒ (ROLE INFO)
    // ========================================================================

    // Mã vai trò của người dùng trong Workspace này.
    // Ví dụ: "WORKSPACE_ADMIN" hoặc "WORKSPACE_MEMBER".
    private String roleCode; 
}