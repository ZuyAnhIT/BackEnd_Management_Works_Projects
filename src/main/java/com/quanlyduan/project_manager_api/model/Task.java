package com.quanlyduan.project_manager_api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity trung tam dai dien cho mot Cong viec (Task/Issue).
 * Quan ly luong cong viec (Workflow), phan cap Agile (Epic/Sprint), 
 * theo doi tien do va tuong tac giua cac thanh vien.
 */
@Getter 
@Setter
@Builder
@Entity
@Table(
    name = "tasks", 
    uniqueConstraints = {
        /** Dam bao Ma Task la duy nhat trong moi Du an (vi du: WN-1, WN-2). */
        @UniqueConstraint(columnNames = {"task_code", "project_id"})
    }
)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include 
    private Integer id;

    // ======================================================
    // 2. PHAN CAP & LIEN KET (HIERARCHY & RELATIONSHIPS)
    // ======================================================
    
    /** Du an chu quan cua Task. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude
    private Project project;

    /** Epic lon chua Task nay (neu co). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epic_id")
    private Epic epic;

    /** Sprint hien tai. Neu null, Task se nam trong Backlog. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    /** * Tham chieu den Task cha. 
     * Ho tro cau truc cay cong viec (Sub-issues). 
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    // ======================================================
    // 3. THONG TIN CO BAN (BASIC INFORMATION)
    // ======================================================
    
    /** Ma Task dinh danh (vi du: "PROJ-101"). */
    @Column(name = "task_code", nullable = false, length = 50)
    private String taskCode;

    /** Tieu de va mo ta chi tiet cong viec. */
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Co danh dau Task da bi luu tru (Archived) hay chua. */
    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived;

    // ======================================================
    // 4. PHAN LOAI & TRANG THAI (CLASSIFICATION & STATUS)
    // ======================================================
    
    /** Loai hinh cong viec (STORY, BUG, TASK). */
    @Enumerated(EnumType.STRING)
    @Column(name = "task_type")
    private TaskType taskType;

    /** * Cot trang thai hien tai tren Board.
     * Lien ket den cau hinh Workflow cua Project. 
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private ProjectStatus status;

    /** Muc do uu tien (LOW, MEDIUM, HIGH, URGENT). */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private TaskPriority priority;

    // ======================================================
    // 5. NHAN SU & CHI SO (PEOPLE & METRICS)
    // ======================================================
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigner_id")
    private User assigner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    /** Do phuc tap theo Agile (Story Points). */
    @Column(name = "story_points")
    private Integer storyPoints;

    /** Thoi gian uoc tinh va thoi gian thuc te da tieu ton. */
    @Column(name = "estimated_hours", precision = 10, scale = 2)
    private BigDecimal estimatedHours;

    @Column(name = "logged_hours", precision = 10, scale = 2)
    private BigDecimal loggedHours;

    // ======================================================
    // 6. DONG THOI GIAN & SAP XEP (TIMELINE & ORDERING)
    // ======================================================
    
    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** Thu tu hien thi tren danh sach hoac bang Kanban. */
    @Column(name = "sort_order")
    private Integer sortOrder;

    // ======================================================
    // 7. THONG TIN HE THONG (AUDIT INFO)
    // ======================================================
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    @ToString.Exclude
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ======================================================
    // 8. QUAN HE PHU (INVERSE RELATIONSHIPS)
    // ======================================================
    
    @OneToMany(mappedBy = "parentTask", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Task> childTasks;

    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<SubTask> subTasks;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<TaskComment> comments;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<TaskAttachment> attachments;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "task_tags",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @ToString.Exclude
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    public Task() {
    }

    public Task(Integer id, Project project, Epic epic, Sprint sprint, Task parentTask, 
                String taskCode, String title, String description, Boolean isArchived, 
                TaskType taskType, ProjectStatus status, TaskPriority priority, 
                User assigner, User assignee, User reviewer, Integer storyPoints, 
                BigDecimal estimatedHours, BigDecimal loggedHours, LocalDateTime startDate, 
                LocalDateTime dueDate, LocalDateTime completedAt, Integer sortOrder, 
                User createdBy, LocalDateTime createdAt, LocalDateTime updatedAt, 
                List<Task> childTasks, List<SubTask> subTasks, List<TaskComment> comments, 
                List<TaskAttachment> attachments, Set<Tag> tags) {
        this.id = id;
        this.project = project;
        this.epic = epic;
        this.sprint = sprint;
        this.parentTask = parentTask;
        this.taskCode = taskCode;
        this.title = title;
        this.description = description;
        this.isArchived = isArchived;
        this.taskType = taskType;
        this.status = status;
        this.priority = priority;
        this.assigner = assigner;
        this.assignee = assignee;
        this.reviewer = reviewer;
        this.storyPoints = storyPoints;
        this.estimatedHours = estimatedHours;
        this.loggedHours = loggedHours;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.completedAt = completedAt;
        this.sortOrder = sortOrder;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.childTasks = childTasks;
        this.subTasks = subTasks;
        this.comments = comments;
        this.attachments = attachments;
        this.tags = tags;
    }
}