// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/ProjectMembershipDTO.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMembershipDTO {
    private Integer projectId;
    private String projectName;
    private Integer workspaceId; // ID của không gian chứa dự án này
    private String roleCode;     // Vai trò trong dự án (ví dụ: PROJECT_MEMBER)
}