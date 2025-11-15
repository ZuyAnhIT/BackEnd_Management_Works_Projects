package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyWorkspaceResponse {

    private Integer workspaceId;
    private Integer companyId;
    private String companyName;
    private String workspaceName;
    private String workspaceCode;
    private String description;
    private String coverImageUrl;
    private String color;
    private String status;
    private String roleCode;
    private String memberStatus;
    private LocalDateTime joinedAt;
}
