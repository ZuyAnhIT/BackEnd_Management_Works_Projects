package com.quanlyduan.project_manager_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;

import com.quanlyduan.project_manager_api.model.common.enums.TaskType;
import com.quanlyduan.project_manager_api.model.common.enums.TaskPriority;

// Import các model đã có
import com.quanlyduan.project_manager_api.model.Project;
import com.quanlyduan.project_manager_api.model.User;
import com.quanlyduan.project_manager_api.model.common.enums.TaskType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title; // Tên của Task

    @Column(columnDefinition = "TEXT")
    private String description;

    // *** QUAN TRỌNG: Liên kết đến Project đã có của bạn ***
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // Sử dụng Project.java bạn đã cung cấp

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id") // Người được giao
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false) // Người tạo
    private User creator;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "task_code", nullable = false)
    private String taskCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private TaskType taskType; // Enum này bạn đã cung cấp (TaskType.java)

    @Column(nullable = false)
    private String status; // CSDL: status VARCHAR(50) DEFAULT 'TO_DO'

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority; // CSDL: priority ENUM(...)

    // --- CÁC QUAN HỆ (Đã có trong code gốc) ---

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubTask> subTasks;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskComment> comments;
}