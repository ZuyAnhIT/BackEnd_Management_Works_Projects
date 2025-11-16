// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/TaskResponse.java
// (MỚI) DTO chung cho Task (chi tiết hơn MyTaskResponse)
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponse {
    private Integer id;
    private String taskCode;
    private String title;
    private String description;
    private String taskType;
    private String status;
    private String priority;
    private Integer storyPoints;
    private LocalDate dueDate;
    
    private Integer projectId;
    private Integer sprintId;
    private Integer epicId;
    
    private Integer assignerId;
    private String assignerName;
    
    private Integer assigneeId;
    private String assigneeName;
    private String assigneeAvatar;

    private Integer createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}
