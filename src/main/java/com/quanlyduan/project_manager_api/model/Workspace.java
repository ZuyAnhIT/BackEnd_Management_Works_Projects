// File: src/main/java/com/quanlyduan/project_manager_api/model/Workspace.java
package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
// Đặt tên bảng là workspaces
@Table(name = "workspaces")
/**
 * Entity đại diện cho một Workspace (Không gian làm việc).
 * Workspace là một container cho các Dự án trong phạm vi một Công ty.
 */
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company; // Công ty sở hữu Workspace này

    @Column(name = "name", nullable = false)
    private String name; // Tên Workspace

    @Column(name = "workspace_code")
    private String workspaceCode; // Mã code Workspace (Ví dụ: HR, DEV)

    @Column(name = "description")
    private String description; // Mô tả Workspace

    @Column(name = "cover_image_url")
    private String coverImageUrl; // URL ảnh bìa

    @Builder.Default
    @Column(name = "color")
    private String color = "#3498db"; // Mã màu chủ đạo (Mặc định là #3498db)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy; // Người dùng đã tạo Workspace này

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkspaceStatus status = WorkspaceStatus.ACTIVE; // Trạng thái (ACTIVE, ARCHIVED, DELETED)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng
}