// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateTaskRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;

    @NotNull(message = "Loại công việc (taskType) không được để trống")
    private TaskType taskType; // (ví dụ: TASK, BUG, STORY)

    private TaskPriority priority; 
    private Integer sprintId; 
    private Integer epicId;
    private Integer assigneeId; // ID của người được gán
    private Integer storyPoints;
    private LocalDate dueDate;
}