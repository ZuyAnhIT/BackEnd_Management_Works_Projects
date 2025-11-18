// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/MyTaskResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class MyTaskResponse {
    private Integer taskId;
    private String taskCode;
    private String taskTitle;
    
    private Integer taskStatusId;   
    private String taskStatusName;  
    private String taskStatusColor; 

    private TaskPriority taskPriority;
    private LocalDate taskDueDate;
    
    private Integer projectId;
    private String projectName;
    private Integer workspaceId;
    private String workspaceName;
}