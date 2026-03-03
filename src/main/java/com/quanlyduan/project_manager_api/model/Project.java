package com.quanlyduan.project_manager_api.model;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.quanlyduan.project_manager_api.model.common.enums.ProjectPriority;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;

/**
 * Entity đại diện cho một Dự án.
 */
@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh dự án

    // ==========================================
    // RELATIONSHIPS (Quan hệ Entity)
    // ==========================================
    // Mối quan hệ: Dự án thuộc về một Workspace
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    // Mối quan hệ: Loại dự án (ví dụ: Marketing, Software)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_type_id")
    private ProjectType projectType;

    // Mối quan hệ: Người quản lý dự án (Project Manager)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    // ==========================================
    // BASIC INFORMATION (Thông tin cơ bản)
    // ==========================================
    @Column(name = "name", nullable = false)
    private String name; // Tên dự án

    @Column(name = "project_code", nullable = false)
    private String projectCode; // Mã dự án duy nhất (ví dụ: WEB)

    @Column(name = "description")
    private String description; // Mô tả dự án

    @Column(name = "goal")
    private String goal; // Mục tiêu của dự án

    // ==========================================
    // STATUS & PRIORITY (Trạng thái & Độ ưu tiên)
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.NEW; // Trạng thái dự án (Mặc định: NEW)

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    @Builder.Default
    private ProjectPriority priority = ProjectPriority.MEDIUM; // Độ ưu tiên (Mặc định: MEDIUM)

    // ==========================================
    // TIMELINE & METRICS (Dòng thời gian & Tiến độ)
    // ==========================================
    @Column(name = "start_date")
    private LocalDate startDate; // Ngày bắt đầu dự kiến

    @Column(name = "due_date")
    private LocalDate dueDate; // Ngày đến hạn dự kiến

    @Column(name = "completed_at")
    private LocalDate completedAt; // Ngày dự án thực sự hoàn thành

    @Column(name = "progress")
    private BigDecimal progress; // Tiến độ dự án (dạng số thập phân)

    // ==========================================
    // UI & CONFIGURATION (Giao diện & Cấu hình)
    // ==========================================
    @Column(name = "cover_image_url")
    private String coverImageUrl; // BỔ SUNG: Cột URL ảnh bìa

    @Column(name = "board_config", columnDefinition = "JSON")
    private String boardConfig; // BỔ SUNG: Cột cấu hình bảng Kanban/Scrum (Lưu dưới dạng JSON String)

    // ==========================================
    // AUDIT & TIMESTAMPS (Hệ thống & Thời gian)
    // ==========================================
    // Mối quan hệ: Người tạo dự án
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Audit Field: Thời điểm tạo

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Audit Field: Thời điểm cập nhật cuối cùng

}