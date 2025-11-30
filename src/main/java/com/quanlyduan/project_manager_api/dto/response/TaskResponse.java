// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/TaskResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO phản hồi thông tin chi tiết đầy đủ của một Công việc (Task).
 * Dùng cho API: GET /api/tasks/{taskId}
 * Cấu trúc đã được nâng cấp để trả về JSON dạng lồng nhau (Nested Objects).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    // ========================================================================
    // 1. ĐỊNH DANH & NỘI DUNG (IDENTITY & CONTENT)
    // ========================================================================
    private Integer id;
    private String taskCode;    // Ví dụ: WEB-01
    private String title;
    private String description; // Chi tiết mô tả

    // ========================================================================
    // 2. PHÂN LOẠI (CLASSIFICATION)
    // ========================================================================
    private TaskType taskType;     // Enum: STORY, BUG, TASK...
    private TaskPriority priority; // Enum: URGENT, HIGH...

    // ========================================================================
    // 3. THÔNG TIN CẤU TRÚC (HIERARCHY & STATUS) - NESTED OBJECTS
    // ========================================================================
    
    // Trạng thái (Cột trên Board)
    private StatusInfo status;

    // Dự án chứa Task
    private ProjectInfo project;

    // Sprint hiện tại (nếu có)
    private SprintInfo sprint;

    // Epic liên quan (nếu có)
    private EpicInfo epic;

    // Task cha (nếu đây là Subtask hoặc Task con)
    private TaskInfo parentTask;

    // ========================================================================
    // 4. NHÂN SỰ (PERSONNEL) - NESTED OBJECTS
    // ========================================================================
    
    // Người thực hiện (Assignee)
    private UserInfo assignee;

    // Người giao việc (Assigner)
    private UserInfo assigner;

    // Người review (Reviewer - nếu có quy trình này)
    private UserInfo reviewer;

    // ========================================================================
    // 5. THÔNG TIN BỔ SUNG (EXTRAS)
    // ========================================================================
    
    // Danh sách thẻ (Tags)
    private List<TagInfo> tags;

    // Tóm tắt tiến độ công việc con (Ví dụ: 2/5 hoàn thành)
    private SubtaskSummary subtaskSummary;

    // ========================================================================
    // 6. METRICS & THỜI GIAN (TIMELINE)
    // ========================================================================
    private Integer storyPoints;       // Điểm độ khó
    private BigDecimal estimatedHours; // Giờ ước tính
    private BigDecimal loggedHours;    // Giờ đã log

    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;

    // ========================================================================
    // 7. AUDIT INFO
    // ========================================================================
    private Integer createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ========================================================================
    // INNER CLASSES (DTO CON)
    // ========================================================================

    @Data @Builder
    public static class StatusInfo {
        private Integer id;
        private String name;
        private String color;
        private Boolean isCompleted;
    }

    @Data @Builder
    public static class ProjectInfo {
        private Integer id;
        private String name;
    }

    @Data @Builder
    public static class SprintInfo {
        private Integer id;
        private String name;
    }

    @Data @Builder
    public static class EpicInfo {
        private Integer id;
        private String name;
        private String color;
    }

    @Data @Builder
    public static class TaskInfo {
        private Integer id;
        private String taskCode;
        private String title;
    }

    @Data @Builder
    public static class UserInfo {
        private Integer id;
        private String name;
        private String avatarUrl;
    }

    @Data @Builder
    public static class TagInfo {
        private Integer id;
        private String name;
        private String color;
    }

    @Data @Builder
    public static class SubtaskSummary {
        private int total;
        private int completed;
    }
}