// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/CreateTaskRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO nhận dữ liệu khi tạo mới một Công việc (Task).
 * Thiết kế linh hoạt để hỗ trợ cả "Tạo nhanh" (chỉ cần Title) và "Tạo đầy đủ".
 */
@Data
public class CreateTaskRequest {

    // Tiêu đề công việc (Bắt buộc, không được để trống)
    @NotBlank(message = "Task title must not be blank") 
    private String title;

    // Mô tả chi tiết công việc (Tùy chọn)
    private String description;

    // Loại công việc (Tùy chọn).
    // Nếu null, Service sẽ tự động gán mặc định là TASK.
    private TaskType taskType; 

    // Mức độ ưu tiên (Tùy chọn).
    // Nếu null, Service sẽ tự động gán mặc định là MEDIUM.
    private TaskPriority priority; 
    
    // ID của Sprint chứa task này (Tùy chọn).
    // - Có giá trị: Task được thêm vào Sprint đó.
    // - Null: Task được thêm vào Backlog.
    private Integer sprintId; 

    // Các thông tin bổ sung khác (Tùy chọn)
    private Integer epicId;      // Gán vào Epic
    private Integer assigneeId;  // Người được giao việc
    private Integer storyPoints; // Điểm ước lượng (Scrum)
    private LocalDateTime dueDate; // Hạn chót
}