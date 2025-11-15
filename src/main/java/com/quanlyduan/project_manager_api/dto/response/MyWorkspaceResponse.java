package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO hiển thị thông tin workspace mà người dùng là thành viên.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyWorkspaceResponse {

    private Integer workspaceId;
    private String workspaceName;
    private String workspaceCode;
    private String workspaceDescription;
    private String workspaceCoverImage;
    private String workspaceColor;
    private String workspaceStatus;

    private Integer companyId;
    private String companyName;
    private String companyLogoUrl;

    private String roleCode;
    private String roleName;

    private MemberStatus membershipStatus;
    private LocalDateTime joinedAt;
}
