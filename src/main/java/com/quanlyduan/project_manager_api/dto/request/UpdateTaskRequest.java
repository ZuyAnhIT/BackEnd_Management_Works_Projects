// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateTaskRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UpdateTaskRequest {
    // Các trường này là tùy chọn (Optional).
    // Nếu client gửi null -> Giữ nguyên giá trị cũ.
    
    private String title;
    private String description;
    
    private TaskType taskType;
    private TaskPriority priority;
    
    private Integer statusId; // Cập nhật cột trạng thái
    
    private Integer sprintId; // 0 hoặc null có thể dùng để gỡ khỏi sprint (tùy logic)
    private Integer epicId;
    private Integer assigneeId; // Người thực hiện
    
    private Integer storyPoints;
    private BigDecimal estimatedHours;
    
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
}