// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/WorkspaceResponse.java
package com.quanlyduan.project_manager_api.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceResponse {
    private Integer workspaceId; // Đã dịch
    private Integer companyId; // Đã dịch
    private String workspaceName; // Đã dịch
    private String description; // Đã dịch
    private String coverImage; // Đã dịch
    private String color; // Đã dịch
    private Integer createdById; // Đã dịch
    private String status; // Đã dịch
    private LocalDateTime createdAt; // Đã dịch
}
