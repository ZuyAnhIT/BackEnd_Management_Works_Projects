// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateWorkspaceRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateWorkspaceRequest {

    @NotBlank(message = "Tên không gian làm việc không được để trống") // Đã dịch
    private String workspaceName; // Đã dịch

    private String description; // Đã dịch
    private String coverImage; // Đã dịch
    private String color; // Đã dịch (vd: #3498db)
}