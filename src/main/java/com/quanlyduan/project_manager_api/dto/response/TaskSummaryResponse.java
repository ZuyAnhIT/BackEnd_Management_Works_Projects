// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/TaskSummaryResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class TaskSummaryResponse {
    private Integer id;
    private String taskCode;
    private String title;
    private TaskType taskType;
    
    private Integer statusId; // ID của trạng thái
    private String statusName; // Tên trạng thái (ví dụ: "Cần làm")
    private String statusColor; // Mã màu (ví dụ: "#808080")
    
    private TaskPriority priority;
    private Integer sprintId;
    
    // Thông tin người được gán (Assignee)
    private Integer assigneeId;
    private String assigneeName;
    private String assigneeAvatarUrl;
    
    // Thông tin Epic (nếu có)
    private Integer epicId;
    private String epicName;
    private String epicColor;

    private Integer storyPoints;
    private LocalDate dueDate;
    private Integer sortOrder;
}