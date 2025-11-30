// File: src/main/java/com/quanlyduan/project_manager_api/model/ProjectType.java
package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.ProjectModel; // Import Enum
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_types") // Đặt tên bảng là project_types
/**
 * Entity đại diện cho Loại Dự án (ví dụ: Phát triển phần mềm, Marketing, Xây dựng).
 */
public class ProjectType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName; // Tên loại dự án (Ví dụ: "Phần mềm Scrum")

    @Column(name = "type_code", unique = true, length = 50)
    private String typeCode; // Mã loại (Ví dụ: "SCRUM_SW")

    // Map cột ENUM của CSDL sang Enum ProjectModel của Java
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectModel model; // Mô hình quản lý dự án áp dụng (SCRUM, KANBAN, etc.)

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả loại dự án

    // Cột cấu hình bổ sung (Lưu trữ dưới dạng JSON String trong DB)
    // Logic nghiệp vụ sẽ chịu trách nhiệm parse chuỗi này.
    @Column(columnDefinition = "JSON")
    private String configuration;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo
}