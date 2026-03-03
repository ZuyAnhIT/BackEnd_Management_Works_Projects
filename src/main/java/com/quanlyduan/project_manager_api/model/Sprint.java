package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;
import java.util.Set;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// Lombok
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Project Enums
import com.quanlyduan.project_manager_api.model.common.enums.SprintStatus;

/**
 * Entity đại diện cho một Sprint trong quy trình Scrum/Agile.
 * Sprint là một khoảng thời gian cố định để hoàn thành một lượng công việc đã chọn.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sprints")
public class Sprint {

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    // ==========================================
    // RELATIONSHIPS (Quan hệ Entity - Owning Side)
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // Dự án chứa Sprint này

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdBy; // Người tạo Sprint

    // ==========================================
    // BASIC INFORMATION (Thông tin cơ bản)
    // ==========================================
    @Column(name = "name", nullable = false)
    private String name; // Tên Sprint (Ví dụ: Sprint 1, Q3-2025)

    @Column(name = "sprint_code", length = 50)
    private String sprintCode; // Mã code Sprint (Ví dụ: PROJ-S1)

    @Column(name = "goal", columnDefinition = "TEXT")
    private String goal; // Mục tiêu của Sprint này

    // ==========================================
    // STATUS & TIMELINE (Trạng thái & Dòng thời gian)
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SprintStatus status; // Trạng thái Sprint (NOT_STARTED, IN_PROGRESS, COMPLETED, CANCELLED)

    @Column(name = "start_date")
    private LocalDateTime startDate; // Ngày bắt đầu thực tế/dự kiến

    @Column(name = "end_date")
    private LocalDateTime endDate; // Ngày kết thúc thực tế/dự kiến

    @Column(name = "duration_days")
    private Integer durationDays; // Độ dài Sprint theo ngày (Ví dụ: 14 ngày)

    // ==========================================
    // TIMESTAMPS (Thời gian hệ thống)
    // ==========================================
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng

    // ==========================================
    // INVERSE RELATIONSHIPS (Quan hệ nghịch đảo)
    // ==========================================
    // MappedBy trỏ đến tên thuộc tính "sprint" trong Entity Task
    @OneToMany(mappedBy = "sprint")
    private Set<Task> tasks; // Quan hệ 1-N: Một Sprint có nhiều Task

}