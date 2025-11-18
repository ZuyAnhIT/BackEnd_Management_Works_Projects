// File: src/main/java/com/quanlyduan/project_manager_api/model/ProjectStatus.java
package com.quanlyduan.project_manager_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_statuses", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"project_id", "name"})
})
public class ProjectStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "name", nullable = false, length = 100)
    private String name; // Tên cột (ví dụ: "Cần làm")

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder; // Thứ tự cột

    @Column(name = "is_completed_status", nullable = false)
    private Boolean isCompletedStatus = false; // Đây có phải cột "Hoàn thành"?
}