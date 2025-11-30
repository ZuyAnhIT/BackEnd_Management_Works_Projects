// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/TaskResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO phản hồi thông tin chi tiết đầy đủ của một Công việc (Task).
 * Dùng cho các API xem chi tiết Task (GET /api/tasks/{taskId}).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    // ========================================================================
    // 1. ĐỊNH DANH & CẤP BẬC (IDENTITY & HIERARCHY)
    // ========================================================================

    // ID định danh của Task
    private Integer id;
    
    // Mã Task (ví dụ: WEB-01, MKTG-05)
    private String taskCode;

    // ID của Dự án chứa Task này
    private Integer projectId;
    
    // ID của Sprint chứa Task này (hoặc null nếu ở Backlog)
    private Integer sprintId;
    
    // ID của Epic liên quan
    private Integer epicId;

    // ========================================================================
    // 2. NỘI DUNG & PHÂN LOẠI (CONTENT & CLASSIFICATION)
    // ========================================================================

    // Tiêu đề của Task
    private String title;
    
    // Mô tả chi tiết của Task
    private String description;

    // Loại công việc (ví dụ: "TASK", "BUG", "STORY")
    private String taskType;

    // Mức độ ưu tiên (ví dụ: "HIGH", "MEDIUM")
    private String priority;

    // ========================================================================
    // 3. TRẠNG THÁI & CẤU HÌNH (STATUS & CONFIG)
    // ========================================================================

    // ID của trạng thái/cột (Khóa ngoại ProjectStatus)
    private Integer statusId;

    // Tên trạng thái hiển thị (ví dụ: "In Progress")
    private String statusName;

    // Mã màu trạng thái
    private String statusColor; 

    // ========================================================================
    // 4. ƯỚC LƯỢNG & THỜI GIAN (METRICS & TIMELINE)
    // ========================================================================

    // Điểm câu chuyện (Story Points)
    private Integer storyPoints;

    // Thời gian ước tính (giờ)
    private BigDecimal estimatedHours;

    // Thời gian đã ghi nhận thực tế (giờ)
    private BigDecimal loggedHours;

    // Thời gian bắt đầu thực tế/dự kiến
    private LocalDateTime startDate;

    // Hạn chót
    private LocalDateTime dueDate;

    // Thời gian hoàn thành thực tế
    private LocalDateTime completedAt; 

    // ========================================================================
    // 5. NHÂN SỰ & KIỂM TOÁN (PERSONNEL & AUDIT)
    // ========================================================================

    // Thông tin người GÁN VIỆC
    private Integer assignerId;
    private String assignerName;

    // Thông tin người THỰC HIỆN
    private Integer assigneeId;
    private String assigneeName;
    private String assigneeAvatar;

    // Thông tin người TẠO
    private Integer createdById;
    private String createdByName;
    
    // Thời điểm tạo và cập nhật
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}