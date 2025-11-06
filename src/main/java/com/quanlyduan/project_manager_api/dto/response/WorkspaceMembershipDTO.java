// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/WorkspaceMembershipDTO.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

// DTO con: Chứa thông tin 1 vai trò trong 1 không gian
@Data
@AllArgsConstructor
public class WorkspaceMembershipDTO {
    private Integer workspaceId;
    private String workspaceName; // Đã dịch
    private Integer companyId; // Không gian này thuộc công ty nào
    private String roleCode; // (vd: "WORKSPACE_ADMIN")
}