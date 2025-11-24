// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateTaskRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "Tiêu đề công việc không được để trống") 
    private String title;

    private String description;

    // Bỏ @NotNull. Nếu null, Service sẽ tự gán là TASK
    private TaskType taskType; 

    // Bỏ @NotNull. Nếu null, Service sẽ tự gán là MEDIUM
    private TaskPriority priority; 
    private Integer sprintId; 

    private Integer epicId;
    private Integer assigneeId;
    private Integer storyPoints;
    private LocalDateTime dueDate;
}