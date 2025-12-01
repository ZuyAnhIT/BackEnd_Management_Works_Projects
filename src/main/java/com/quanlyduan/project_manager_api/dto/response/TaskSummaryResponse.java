// File: src/main/java/com/quanlyduan/project_manager_api/dto/response/TaskSummaryResponse.java
package com.quanlyduan.project_manager_api.dto.response;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TaskSummaryResponse {
    private Integer id;
    private String taskCode;
    private String title;
    private TaskType taskType;
    private TaskPriority priority;
    private Integer sprintId;
    private Integer storyPoints;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private Integer sortOrder;

    // --- CÁC OBJECT LỒNG NHAU (NESTED OBJECTS) ---

    private StatusInfo status;     // Thay cho statusId, statusName...
    private EpicInfo epic;         // Thay cho epicId, epicName...
    private UserInfo assignee;     // Thay cho assigneeId...
    
    // --- CÁC TRƯỜNG MỚI BỔ SUNG ---
    private List<TagInfo> tags;
    private SubtaskSummary subtaskSummary;

    // ==========================================
    // Inner Classes (DTO con) để tạo cấu trúc JSON đẹp
    // ==========================================

    @Data
    @Builder
    public static class StatusInfo {
        private Integer id;
        private String name;
        private String color;
    }

    @Data
    @Builder
    public static class EpicInfo {
        private Integer id;
        private String name;
        private String color;
    }

    @Data
    @Builder
    public static class UserInfo {
        private Integer id;
        private String name;
        private String avatarUrl;
    }

    @Data
    @Builder
    public static class TagInfo {
        private Integer id;
        private String name;
        private String color;
    }

    @Data
    @Builder
    public static class SubtaskSummary {
        private int total;
        private int completed;
    }
}