package com.quanlyduan.project_manager_api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin tom tat cua mot Cong viec (Task Summary).
 * Duoc thiet ke toi uu voi cac Nested Objects de render giao dien the (Card) 
 * tren Kanban Board hoac danh sach cong viec.
 */
@Getter
@Setter
@Builder
public class TaskSummaryResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH & PHAN CAP
    // ======================================================
    private Integer id;
    private String taskCode;
    private String title;
    
    private Integer projectId;
    private Integer workspaceId;
    private Integer companyId;

    // ======================================================
    // 2. PHAN LOAI & CHI SO
    // ======================================================
    private TaskType taskType;
    private TaskPriority priority;
    
    private Integer sprintId;
    private Integer storyPoints;
    private Integer sortOrder;

    // ======================================================
    // 3. THOI GIAN
    // ======================================================
    private LocalDateTime startDate;
    private LocalDateTime dueDate;

    // ======================================================
    // 4. THONG TIN LIEN KET (NESTED OBJECTS)
    // ======================================================
    private StatusInfo status;
    private EpicInfo epic;
    private UserInfo assignee;
    
    // ======================================================
    // 5. THONG TIN BO SUNG
    // ======================================================
    private List<TagInfo> tags;
    private SubtaskSummary subtaskSummary;

    // ======================================================
    // INNER CLASSES: NESTED DTOs (RULE 8)
    // ======================================================

    @Getter @Setter @Builder
    public static class StatusInfo {
        private Integer id;
        private String name;
        private String color;

        public StatusInfo() {}
        public StatusInfo(Integer id, String name, String color) {
            this.id = id; this.name = name; this.color = color;
        }
    }

    @Getter @Setter @Builder
    public static class EpicInfo {
        private Integer id;
        private String name;
        private String color;

        public EpicInfo() {}
        public EpicInfo(Integer id, String name, String color) {
            this.id = id; this.name = name; this.color = color;
        }
    }

    @Getter @Setter @Builder
    public static class UserInfo {
        private Integer id;
        private String name;
        private String avatarUrl;

        public UserInfo() {}
        public UserInfo(Integer id, String name, String avatarUrl) {
            this.id = id; this.name = name; this.avatarUrl = avatarUrl;
        }
    }

    @Getter @Setter @Builder
    public static class TagInfo {
        private Integer id;
        private String name;
        private String color;

        public TagInfo() {}
        public TagInfo(Integer id, String name, String color) {
            this.id = id; this.name = name; this.color = color;
        }
    }

    @Getter @Setter @Builder
    public static class SubtaskSummary {
        private int total;
        private int completed;

        public SubtaskSummary() {}
        public SubtaskSummary(int total, int completed) {
            this.total = total; this.completed = completed;
        }
    }

    // ======================================================
    // CONSTRUCTORS DTO CHINH (RULE 5 - TRANSPARENCY)
    // ======================================================

    public TaskSummaryResponse() {
    }

    /**
     * Constructor dung de Builder va Jackson mapping du lieu.
     * NEU DUNG JPQL 'SELECT new...', THU TU TRONG CAU QUERY PHAI KHOP CHINH XAC VOI DAY!
     */
    public TaskSummaryResponse(Integer id, String taskCode, String title, Integer projectId, 
                               Integer workspaceId, Integer companyId, TaskType taskType, 
                               TaskPriority priority, Integer sprintId, Integer storyPoints, 
                               Integer sortOrder, LocalDateTime startDate, LocalDateTime dueDate, 
                               StatusInfo status, EpicInfo epic, UserInfo assignee, 
                               List<TagInfo> tags, SubtaskSummary subtaskSummary) {
        this.id = id;
        this.taskCode = taskCode;
        this.title = title;
        this.projectId = projectId;
        this.workspaceId = workspaceId;
        this.companyId = companyId;
        this.taskType = taskType;
        this.priority = priority;
        this.sprintId = sprintId;
        this.storyPoints = storyPoints;
        this.sortOrder = sortOrder;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.status = status;
        this.epic = epic;
        this.assignee = assignee;
        this.tags = tags;
        this.subtaskSummary = subtaskSummary;
    }
}