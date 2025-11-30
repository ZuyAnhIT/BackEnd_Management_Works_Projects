// File: src/main/java/com/quanlyduan/project_manager_api/model/Project.java
package com.quanlyduan.project_manager_api.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Import đúng Enum ProjectPriority
import com.quanlyduan.project_manager_api.model.common.enums.ProjectPriority;
import com.quanlyduan.project_manager_api.model.common.enums.ProjectStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Entity đại diện cho một Dự án.
 */
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh dự án

    // Mối quan hệ: Dự án thuộc về một Workspace
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    // Mối quan hệ: Loại dự án (ví dụ: Marketing, Software)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_type_id")
    private ProjectType projectType;

    @Column(name = "name", nullable = false)
    private String name; // Tên dự án

    @Column(name = "project_code", nullable = false)
    private String projectCode; // Mã dự án duy nhất (ví dụ: WEB)

    @Column(name = "description")
    private String description; // Mô tả dự án

    @Column(name = "goal")
    private String goal; // Mục tiêu của dự án

    // Mối quan hệ: Người quản lý dự án (Project Manager)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.NEW; // Trạng thái dự án (Mặc định: NEW)

    // *** SỬ DỤNG ENUM ĐÚNG CỦA PROJECT ***
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    @Builder.Default
    private ProjectPriority priority = ProjectPriority.MEDIUM; // Độ ưu tiên (Mặc định: MEDIUM)

    @Column(name = "start_date")
    private LocalDate startDate; // Ngày bắt đầu dự kiến

    @Column(name = "due_date")
    private LocalDate dueDate; // Ngày đến hạn dự kiến

    @Column(name = "completed_at")
    private LocalDate completedAt; // Ngày dự án thực sự hoàn thành

    @Column(name = "progress")
    private BigDecimal progress; // Tiến độ dự án (dạng số thập phân)

    // BỔ SUNG: Cột URL ảnh bìa
    @Column(name = "cover_image_url")
    private String coverImageUrl;

    // BỔ SUNG: Cột cấu hình bảng Kanban/Scrum (Lưu dưới dạng JSON String)
    @Column(name = "board_config", columnDefinition = "JSON")
    private String boardConfig;

    // Mối quan hệ: Người tạo dự án
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    // Audit Field: Thời điểm tạo
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Audit Field: Thời điểm cập nhật cuối cùng
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}