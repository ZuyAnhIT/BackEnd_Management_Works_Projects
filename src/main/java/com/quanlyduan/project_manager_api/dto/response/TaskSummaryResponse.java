// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/TaskSummaryResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO phản hồi thông tin tóm tắt của một Công việc (Task).
 * Dùng cho các danh sách (List View), Board Card, và Backlog để giảm thiểu dữ liệu trả về.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSummaryResponse {

    // ========================================================================
    // 1. ĐỊNH DANH & CỐT LÕI (IDENTITY & CORE INFO)
    // ========================================================================

    // ID định danh của Task
    private Integer id;

    // Mã Task (ví dụ: WEB-01)
    private String taskCode;

    // Tiêu đề của Task
    private String title;

    // ========================================================================
    // 2. PHÂN LOẠI & TRẠNG THÁI (CLASSIFICATION & STATUS)
    // ========================================================================

    // Loại công việc (Enum: TASK, BUG, STORY)
    private TaskType taskType;

    // Mức độ ưu tiên (Enum: HIGH, MEDIUM)
    private TaskPriority priority;

    // ID của trạng thái/cột (Khóa ngoại ProjectStatus)
    private Integer statusId;

    // Tên trạng thái hiển thị (ví dụ: "Cần làm", "In Progress")
    private String statusName;

    // Mã màu trạng thái (dùng cho hiển thị trên Board/Card)
    private String statusColor;

    // ========================================================================
    // 3. THÔNG TIN PHÂN CẤP (HIERARCHY)
    // ========================================================================

    // ID của Sprint chứa Task này (hoặc null nếu ở Backlog)
    private Integer sprintId;

    // ID của Epic liên quan
    private Integer epicId;
    
    // Tên hiển thị của Epic
    private String epicName;

    // Mã màu của Epic
    private String epicColor;

    // ========================================================================
    // 4. NHÂN SỰ (PERSONNEL)
    // ========================================================================

    // ID người được giao việc (Assignee)
    private Integer assigneeId;

    // Tên hiển thị người được giao việc
    private String assigneeName;

    // Đường dẫn Avatar người được giao việc
    private String assigneeAvatarUrl;

    // ========================================================================
    // 5. METRICS & THỜI GIAN (METRICS & TIMELINE)
    // ========================================================================

    // Điểm câu chuyện (Story Points)
    private Integer storyPoints;

    // Hạn chót (Deadline), bao gồm cả giờ phút
    private LocalDateTime dueDate;

    // Thứ tự sắp xếp của Task trong danh sách/cột (Dùng cho kéo thả Ranking)
    private Integer sortOrder;
}