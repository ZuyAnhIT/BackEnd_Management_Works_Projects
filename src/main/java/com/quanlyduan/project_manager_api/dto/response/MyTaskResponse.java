// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/MyTaskResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO hiển thị tất cả các Task được giao cho tôi để dễ dàng quản lý công việc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyTaskResponse {
    // Thông tin Task
    private Integer taskId;
    private String taskCode;
    private String taskTitle;
    private String taskStatus;
    private TaskPriority taskPriority;
    private LocalDate taskDueDate;

    // Thông tin Project
    private Integer projectId;
    private String projectName;

    // Thông tin Workspace
    private Integer workspaceId;
    private String workspaceName;
}
