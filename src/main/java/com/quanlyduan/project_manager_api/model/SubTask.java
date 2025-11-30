// File: src/main/java/com/quanlyduan/project_manager_api/model/SubTask.java
package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sub_tasks")
/**
 * Entity đại diện cho một Subtask (Công việc con) thuộc một Task (Công việc cha).
 */
public class SubTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    // ======================================================
    // MỐI QUAN HỆ VỚI TASK CHA
    // ======================================================
    /**
     * Liên kết Task cha.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id", nullable = false)
    private Task parentTask; // Tham chiếu đến Task cha

    @Column(name = "title", nullable = false, length = 500)
    private String title; // Tiêu đề Subtask

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Mô tả Subtask

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SubTaskStatus status; // Trạng thái Subtask (TO_DO, IN_PROGRESS, DONE)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee; // Người được giao Subtask này

    @Column(name = "estimated_hours", precision = 10, scale = 2)
    private BigDecimal estimatedHours; // Số giờ ước tính hoàn thành

    @Column(name = "sort_order")
    private Integer sortOrder; // Thứ tự sắp xếp trong danh sách Subtask

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdBy; // Người tạo Subtask

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng
}