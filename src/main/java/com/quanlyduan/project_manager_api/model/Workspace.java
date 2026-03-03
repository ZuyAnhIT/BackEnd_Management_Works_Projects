package com.quanlyduan.project_manager_api.model;

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
import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;

/**
 * Entity đại diện cho một Workspace (Không gian làm việc).
 * Workspace là một container cho các Dự án trong phạm vi một Công ty.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "workspaces") // Đặt tên bảng là workspaces
public class Workspace {

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    // ==========================================
    // RELATIONSHIPS (Quan hệ Entity)
    // ==========================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company; // Công ty sở hữu Workspace này

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy; // Người dùng đã tạo Workspace này

    // ==========================================
    // BASIC INFORMATION (Thông tin cơ bản)
    // ==========================================
    @Column(name = "name", nullable = false)
    private String name; // Tên Workspace

    @Column(name = "workspace_code")
    private String workspaceCode; // Mã code Workspace (Ví dụ: HR, DEV)

    @Column(name = "description")
    private String description; // Mô tả Workspace

    // ==========================================
    // UI & STATUS (Giao diện & Trạng thái)
    // ==========================================
    @Column(name = "cover_image_url")
    private String coverImageUrl; // URL ảnh bìa

    @Builder.Default
    @Column(name = "color")
    private String color = "#3498db"; // Mã màu chủ đạo (Mặc định là #3498db)

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkspaceStatus status = WorkspaceStatus.ACTIVE; // Trạng thái (ACTIVE, ARCHIVED, DELETED)

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