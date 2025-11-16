// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateTaskSprintRequest.java
// (MỚI) DTO cho US-S3-7: Kéo/thả Task vào Sprint
package com.quanlyduan.project_manager_api.dto.request;

import lombok.Data;

@Data
public class UpdateTaskSprintRequest {
    // Dùng để kéo thả task vào sprint
    // Nếu sprintId = null, nghĩa là "Move to Backlog"
    private Integer sprintId;
}
