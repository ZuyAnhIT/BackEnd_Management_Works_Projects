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
// Đặt tên bảng là project_statuses
@Table(name = "project_statuses", 
    uniqueConstraints = {
        // Đảm bảo tên trạng thái (cột) là duy nhất trong phạm vi một dự án
        @UniqueConstraint(columnNames = {"project_id", "name"})
    }
)
/**
 * Entity đại diện cho một Trạng thái Task (hoặc một cột trên bảng Kanban/Scrum)
 * tùy chỉnh cho từng Dự án.
 */
public class ProjectStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // Dự án sở hữu trạng thái này

    @Column(name = "name", nullable = false, length = 100)
    private String name; // Tên cột (ví dụ: "Cần làm", "Đang tiến hành", "Đã xong")

    @Column(name = "color", length = 7)
    private String color; // Mã màu hex của cột (ví dụ: #FF0000)

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder; // Thứ tự hiển thị của cột trên bảng (ví dụ: 0, 1, 2)

    @Column(name = "is_completed_status", nullable = false)
    @Builder.Default
    private Boolean isCompletedStatus = false; // Cờ xác định đây có phải là trạng thái "Hoàn thành" (Done/Closed) không
}