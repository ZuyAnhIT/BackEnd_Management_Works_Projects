package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

// JPA & Hibernate
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

// Lombok
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Project Enums
import com.quanlyduan.project_manager_api.model.common.enums.ProjectModel;

/**
 * Entity đại diện cho Loại Dự án (ví dụ: Phát triển phần mềm, Marketing, Xây dựng).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_types") // Đặt tên bảng là project_types
public class ProjectType {

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    // ==========================================
    // BASIC INFORMATION (Thông tin cơ bản)
    // ==========================================
    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName; // Tên loại dự án (Ví dụ: "Phần mềm Scrum")

    @Column(name = "type_code", unique = true, length = 50)
    private String typeCode; // Mã loại (Ví dụ: "SCRUM_SW")

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả loại dự án

    // ==========================================
    // PROJECT MODEL & CONFIGURATION (Mô hình & Cấu hình)
    // ==========================================
    // Map cột ENUM của CSDL sang Enum ProjectModel của Java
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectModel model; // Mô hình quản lý dự án áp dụng (SCRUM, KANBAN, etc.)

    // Cột cấu hình bổ sung (Lưu trữ dưới dạng JSON String trong DB)
    // Logic nghiệp vụ sẽ chịu trách nhiệm parse chuỗi này.
    @Column(columnDefinition = "JSON")
    private String configuration;

    // ==========================================
    // TIMESTAMPS (Thời gian hệ thống)
    // ==========================================
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo

}