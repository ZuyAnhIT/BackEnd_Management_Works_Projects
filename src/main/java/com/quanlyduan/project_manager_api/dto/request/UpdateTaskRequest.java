// File: src/main/java/com/quanlyduan/project_manager_api/dto/request/UpdateTaskRequest.java
package com.quanlyduan.project_manager_api.dto.request;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO nhận dữ liệu cho yêu cầu cập nhật thông tin Công việc (Task).
 * Hỗ trợ cơ chế cập nhật từng phần (Partial Update):
 * - Chỉ những trường có giá trị (không null) mới được cập nhật vào Database.
 * - Giá trị null nghĩa là "không thay đổi".
 */
@Data
public class UpdateTaskRequest {

    // ========================================================================
    // 1. THÔNG TIN CƠ BẢN
    // ========================================================================

    // Tiêu đề mới của Task (Tùy chọn)
    private String title;

    // Mô tả chi tiết mới (Tùy chọn)
    private String description;

    // ========================================================================
    // 2. PHÂN LOẠI & TRẠNG THÁI
    // ========================================================================

    // Loại công việc mới (Enum: TASK, BUG, STORY...)
    private TaskType taskType;

    // Mức độ ưu tiên mới (Enum: LOW, MEDIUM, HIGH, URGENT)
    private TaskPriority priority;
    
    // ID của Trạng thái/Cột mới (Dùng để di chuyển Task sang cột khác trên Board)
    private Integer statusId; 

    // ========================================================================
    // 3. LIÊN KẾT & QUAN HỆ
    // ========================================================================
    
    // ID của Sprint.
    // - Logic xử lý trong Service:
    //   + Null: Giữ nguyên Sprint hiện tại.
    //   + 0: Gỡ Task khỏi Sprint hiện tại (Đưa về Backlog).
    //   + > 0: Chuyển Task sang Sprint có ID tương ứng.
    private Integer sprintId; 

    // ID của Epic liên quan (Tùy chọn, logic tương tự SprintId nếu cần gỡ bỏ)
    private Integer epicId;

    // ID của người được giao việc (Assignee)
    private Integer assigneeId; 
    
    // ========================================================================
    // 4. ƯỚC LƯỢNG & THỜI GIAN
    // ========================================================================

    // Điểm câu chuyện (Story Points - dùng cho Scrum)
    private Integer storyPoints;

    // Thời gian ước tính để hoàn thành (đơn vị: giờ)
    private BigDecimal estimatedHours;
    
    // Thời gian bắt đầu dự kiến
    private LocalDateTime startDate;

    // Hạn chót hoàn thành (Deadline)
    private LocalDateTime dueDate;
}