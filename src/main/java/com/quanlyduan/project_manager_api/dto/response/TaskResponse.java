// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/TaskResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponse {
    private Integer id;
    private String taskCode;
    private String title;
    private String description;
    
    private Integer statusId;    
    private String statusName;  
    private String statusColor; 

    private String taskType; // (ví dụ: "TASK", "BUG")
    private String priority; // (ví dụ: "HIGH", "MEDIUM")
    
    private Integer storyPoints;
    private BigDecimal estimatedHours; 
    private BigDecimal loggedHours; 

    private LocalDate startDate; 
    private LocalDate dueDate;
    private LocalDate completedAt; 

    private Integer projectId;
    private Integer sprintId;
    private Integer epicId;
    
    // Thông tin người gán (Assigner)
    private Integer assignerId;
    private String assignerName;

    // Thông tin người được gán (Assignee)
    private Integer assigneeId;
    private String assigneeName;
    private String assigneeAvatar;

    // Thông tin người tạo
    private Integer createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}