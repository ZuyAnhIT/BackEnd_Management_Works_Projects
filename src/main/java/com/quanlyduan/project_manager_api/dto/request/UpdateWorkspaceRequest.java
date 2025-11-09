// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateWorkspaceRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class UpdateWorkspaceRequest {

    @Size(min = 1, max = 255, message = "Workspace name must be between 1 and 255 characters")
    private String name; // Tên mới
    private String description;
    private String coverImage;
    private String color;
}