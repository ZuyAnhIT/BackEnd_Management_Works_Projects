package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa thông tin tóm tắt về Project mà người dùng tham gia.
 * Được sử dụng trong API /dashboard/my-projects
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyProjectResponse {
    // Thông tin Project
    private Integer projectId;
    private String projectName;
    private String description;
    private String coverImage;
    private String color;

    // Thông tin cha (Workspace & Company)
    private Integer workspaceId;
    private String workspaceName;
    private Integer companyId;
    private String companyName;

    // Vai trò của người dùng hiện tại trong project
    private String myRoleName;
}