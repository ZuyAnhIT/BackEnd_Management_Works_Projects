// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/AssignTaskRequest.java

package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignTaskRequest {
    @NotNull(message = "Assignee ID must not be null")
    private Integer assigneeId; // ID của người được gán
}
