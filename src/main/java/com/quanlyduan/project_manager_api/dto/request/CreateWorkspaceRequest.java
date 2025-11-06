// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateWorkspaceRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateWorkspaceRequest {

    @NotBlank(message = "Workspace name must not be blank") // Đã dịch
    private String workspaceName; // Đã dịch

    private String description; // Đã dịch
    private String coverImage; // Đã dịch
    private String color; // Đã dịch (vd: #3498db)
}