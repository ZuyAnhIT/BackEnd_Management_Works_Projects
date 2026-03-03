package com.quanlyduan.project_manager_api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// JPA & Hibernate
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// Lombok
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

// Project Enums
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;

/**
 * Entity đại diện cho một Công việc/Task (có thể là Story, Bug, Task thường).
 */
@Getter 
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "tasks", 
    uniqueConstraints = {
        // Đảm bảo task_code là duy nhất trong phạm vi một project
        @UniqueConstraint(columnNames = {"task_code", "project_id"})
    }
)
// QUAN TRỌNG: Chỉ tính hashCode/equals dựa trên ID để tránh đệ quy vô hạn
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task {

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // Chỉ dùng ID để so sánh và tính hash
    private Integer id; // ID định danh

    // ==========================================
    // HIERARCHY & RELATIONSHIPS (Quan hệ cấp bậc)
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude // Ngắt vòng lặp log
    private Project project; // Dự án chứa Task này

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epic_id")
    private Epic epic; // Epic (nếu có)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint; // Sprint (Nếu null -> nằm ở Backlog)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask; // Tham chiếu đến Task cha (nếu là Task con)

    // ==========================================
    // BASIC INFORMATION (Thông tin cơ bản)
    // ==========================================
    @Column(name = "task_code", nullable = false, length = 50)
    private String taskCode; // Mã Task (Ví dụ: PROJ-123)

    @Column(name = "title", nullable = false, length = 500)
    private String title; // Tiêu đề Task

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Mô tả Task

    @Builder.Default
    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived = false; // Trạng thái lưu trữ

    // ==========================================
    // CLASSIFICATION & STATUS (Phân loại & Trạng thái)
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "task_type")
    private TaskType taskType; // Loại Task (STORY, BUG, TASK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private ProjectStatus status; // Trạng thái/Cột (Link đến bảng ProjectStatus)

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private TaskPriority priority; // Độ ưu tiên

    // ==========================================
    // PEOPLE & METRICS (Nhân sự & Chỉ số)
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigner_id")
    private User assigner; // Người giao việc

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee; // Người được giao việc

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer; // Người đánh giá/Review

    @Column(name = "story_points")
    private Integer storyPoints; // Story Points (độ phức tạp trong Agile)

    @Column(name = "estimated_hours", precision = 10, scale = 2)
    private BigDecimal estimatedHours; // Số giờ ước tính

    @Column(name = "logged_hours", precision = 10, scale = 2)
    private BigDecimal loggedHours; // Số giờ đã ghi nhận (Time tracking)

    // ==========================================
    // TIMELINE & TRACKING (Dòng thời gian & Theo dõi)
    // ==========================================
    @Column(name = "start_date")
    private LocalDateTime startDate; // Ngày bắt đầu

    @Column(name = "due_date")
    private LocalDateTime dueDate; // Ngày đến hạn

    @Column(name = "completed_at")
    private LocalDateTime completedAt; // Thời điểm hoàn thành

    @Column(name = "sort_order")
    private Integer sortOrder; // Thứ tự sắp xếp (trên Board/Backlog)

    // ==========================================
    // TIMESTAMPS & AUDIT (Hệ thống)
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    @ToString.Exclude
    private User createdBy; // Người tạo Task

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng

    // ==========================================
    // INVERSE RELATIONSHIPS (Quan hệ nghịch đảo)
    // ==========================================
    // List các Task con (nếu đây là Task cha)
    @OneToMany(mappedBy = "parentTask")
    @ToString.Exclude
    private List<Task> childTasks;

    // List các SubTask (công việc con nhẹ hơn Task)
    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<SubTask> subTasks;

    // List các Bình luận
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<TaskComment> comments;

    // List các Tệp đính kèm
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<TaskAttachment> attachments;

    // Tags (Quan hệ Many-to-Many với Tag)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "task_tags", // Tên bảng trung gian
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @ToString.Exclude // Ngắt vòng lặp log
    @Builder.Default // Khởi tạo HashSet để tránh NullPointerException khi add tag
    private Set<Tag> tags = new HashSet<>();

}