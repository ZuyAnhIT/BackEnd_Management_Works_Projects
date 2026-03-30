package com.quanlyduan.project_manager_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin chi tiet toan dien cua mot Cong viec (Task).
 * Su dung cau truc Nested Objects de cung cap day du ngu canh (Context) cho Frontend 
 * chi trong mot lan goi API (GET /api/tasks/{taskId}).
 */
@Getter
@Setter
@Builder
public class TaskResponse {

    // ======================================================
    // 1. DINH DANH & NOI DUNG (IDENTITY & CONTENT)
    // ======================================================
    
    /** ID dinh danh duy nhat cua Task. */
    private Integer id;
    
    /** Ma code hien thi (vi du: "WEB-01"). */
    private String taskCode;
    
    /** Ngu canh phan cap de quan ly bao mat. */
    private Integer workspaceId;
    private Integer companyId;
    
    /** Tieu de va Noi dung chi tiet cua cong viec. */
    private String title;
    private String description;

    // ======================================================
    // 2. PHAN LOAI (CLASSIFICATION)
    // ======================================================
    
    /** Loai cong viec (vi du: STORY, BUG, TASK). */
    private TaskType taskType;
    
    /** Muc do uu tien (vi du: LOW, HIGH, URGENT). */
    private TaskPriority priority;

    // ======================================================
    // 3. CAU TRUC & TRANG THAI (HIERARCHY & STATUS)
    // ======================================================
    
    /** Thong tin Trang thai hien tai (Cot tren Board). */
    private StatusInfo status;

    /** Thong tin Du an chua Task nay. */
    private ProjectInfo project;

    /** Thong tin Sprint hien tai (neu co). */
    private SprintInfo sprint;

    /** Thong tin Epic lien quan (neu co). */
    private EpicInfo epic;

    /** Thong tin Task cha (neu day la mot Subtask). */
    private TaskInfo parentTask;

    // ======================================================
    // 4. NHAN SU (PERSONNEL)
    // ======================================================
    
    /** Nguoi chiu trach nhiem thuc hien (Assignee). */
    private UserInfo assignee;

    /** Nguoi giao viec (Assigner/Reporter). */
    private UserInfo assigner;

    /** Nguoi kiem duyet/Review (neu co). */
    private UserInfo reviewer;

    // ======================================================
    // 5. THONG TIN BO SUNG (EXTRAS)
    // ======================================================
    
    /** Danh sach cac Nhan (Tags) duoc gan vao Task. */
    private List<TagInfo> tags;

    /** Tom tat tin do cong viec con (vi du: 2/5 Subtask hoan thanh). */
    private SubtaskSummary subtaskSummary;

    // ======================================================
    // 6. THOI GIAN & CHI SO (METRICS & TIMELINE)
    // ======================================================
    
    /** Diem do kho hoac khoi luong cong viec. */
    private Integer storyPoints;
    
    /** Thoi gian uoc tinh va Thoi gian thuc te da log (don vi: Gio). */
    private BigDecimal estimatedHours;
    private BigDecimal loggedHours;

    /** Cac cot moc thoi gian quan trong. */
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;

    // ======================================================
    // 7. THONG TIN HE THONG (AUDIT INFO)
    // ======================================================
    
    private Integer createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ======================================================
    // INNER CLASSES: NESTED DTOs (RULE 8)
    // ======================================================

    @Getter @Setter @Builder
    public static class StatusInfo {
        private Integer id;
        private String name;
        private String color;
        private Boolean isCompleted;

        public StatusInfo() {}
        public StatusInfo(Integer id, String name, String color, Boolean isCompleted) {
            this.id = id; this.name = name; this.color = color; this.isCompleted = isCompleted;
        }
    }

    @Getter @Setter @Builder
    public static class ProjectInfo {
        private Integer id;
        private String name;

        public ProjectInfo() {}
        public ProjectInfo(Integer id, String name) { this.id = id; this.name = name; }
    }

    @Getter @Setter @Builder
    public static class SprintInfo {
        private Integer id;
        private String name;

        public SprintInfo() {}
        public SprintInfo(Integer id, String name) { this.id = id; this.name = name; }
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
    public static class TaskInfo {
        private Integer id;
        private String taskCode;
        private String title;

        public TaskInfo() {}
        public TaskInfo(Integer id, String taskCode, String title) {
            this.id = id; this.taskCode = taskCode; this.title = title;
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

    public TaskResponse() {
    }

    public TaskResponse(Integer id, String taskCode, Integer workspaceId, Integer companyId, 
                        String title, String description, TaskType taskType, TaskPriority priority, 
                        StatusInfo status, ProjectInfo project, SprintInfo sprint, EpicInfo epic, 
                        TaskInfo parentTask, UserInfo assignee, UserInfo assigner, 
                        UserInfo reviewer, List<TagInfo> tags, SubtaskSummary subtaskSummary, 
                        Integer storyPoints, BigDecimal estimatedHours, BigDecimal loggedHours, 
                        LocalDateTime startDate, LocalDateTime dueDate, LocalDateTime completedAt, 
                        Integer createdById, String createdByName, LocalDateTime createdAt, 
                        LocalDateTime updatedAt) {
        this.id = id;
        this.taskCode = taskCode;
        this.workspaceId = workspaceId;
        this.companyId = companyId;
        this.title = title;
        this.description = description;
        this.taskType = taskType;
        this.priority = priority;
        this.status = status;
        this.project = project;
        this.sprint = sprint;
        this.epic = epic;
        this.parentTask = parentTask;
        this.assignee = assignee;
        this.assigner = assigner;
        this.reviewer = reviewer;
        this.tags = tags;
        this.subtaskSummary = subtaskSummary;
        this.storyPoints = storyPoints;
        this.estimatedHours = estimatedHours;
        this.loggedHours = loggedHours;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.completedAt = completedAt;
        this.createdById = createdById;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}