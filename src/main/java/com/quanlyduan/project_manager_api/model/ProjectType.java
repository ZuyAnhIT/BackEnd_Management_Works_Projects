// File: src/main/java/com/quanlyduan/project_manager_api/model/ProjectType.java
package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.ProjectModel; // Import file Enum vừa tạo
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
@Table(name = "project_types") // Khớp với bảng CSDL (mục 13)
public class ProjectType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName;

    @Column(name = "type_code", unique = true, length = 50)
    private String typeCode;

    // Map cột ENUM của CSDL sang Enum của Java
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectModel model;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Map cột JSON sang String.
    // Logic nghiệp vụ (ví dụ: dùng Jackson) sẽ chịu trách nhiệm parse chuỗi này.
    @Column(columnDefinition = "JSON")
    private String configuration;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}