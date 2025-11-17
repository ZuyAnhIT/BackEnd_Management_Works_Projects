// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateTaskRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@Builder
@Data
public class CreateTaskRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;
    
    // (Optional) ID của Sprint. Nếu null, task sẽ rơi vào Backlog
    private Integer sprintId; 
    
    // (Optional) ID của Epic
    private Integer epicId; 
    
    // (Optional) ID của người được gán (assignee)
    private Integer assigneeId; 
    
    // (Optional) Trạng thái ban đầu (ví dụ: 'TO_DO')
    private String status; 

    @Builder.Default
    private TaskType taskType = TaskType.TASK;

    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    private Integer storyPoints;
    private BigDecimal estimatedHours;
    private LocalDate startDate;
    private LocalDate dueDate;
}