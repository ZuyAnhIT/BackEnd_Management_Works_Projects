package com.quanlyduan.project_manager_api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// JPA & Hibernate
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// Lombok
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Project Enums
import com.quanlyduan.project_manager_api.model.common.enums.SubTaskStatus;

/**
 * Entity đại diện cho một Subtask (Công việc con) thuộc một Task (Công việc cha).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sub_tasks")
public class SubTask {

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    // ==========================================
    // RELATIONSHIPS (Quan hệ Entity)
    // ==========================================
    /**
     * Liên kết Task cha.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id", nullable = false)
    private Task parentTask; // Tham chiếu đến Task cha

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee; // Người được giao Subtask này

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdBy; // Người tạo Subtask

    // ==========================================
    // BASIC INFORMATION (Thông tin cơ bản)
    // ==========================================
    @Column(name = "title", nullable = false, length = 500)
    private String title; // Tiêu đề Subtask

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Mô tả Subtask

    // ==========================================
    // STATUS & ESTIMATION (Trạng thái & Ước lượng)
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SubTaskStatus status; // Trạng thái Subtask (TO_DO, IN_PROGRESS, DONE)

    @Column(name = "estimated_hours", precision = 10, scale = 2)
    private BigDecimal estimatedHours; // Số giờ ước tính hoàn thành

    // ==========================================
    // DISPLAY & ORDERING (Hiển thị & Sắp xếp)
    // ==========================================
    @Column(name = "sort_order")
    private Integer sortOrder; // Thứ tự sắp xếp trong danh sách Subtask

    // ==========================================
    // TIMESTAMPS (Thời gian hệ thống)
    // ==========================================
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng

}