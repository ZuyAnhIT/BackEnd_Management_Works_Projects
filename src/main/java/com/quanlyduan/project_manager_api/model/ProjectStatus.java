package com.quanlyduan.project_manager_api.model;

// JPA & Hibernate
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// Lombok
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity đại diện cho một Trạng thái Task (hoặc một cột trên bảng Kanban/Scrum)
 * tùy chỉnh cho từng Dự án.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "project_statuses", 
    uniqueConstraints = {
        // Đảm bảo tên trạng thái (cột) là duy nhất trong phạm vi một dự án
        @UniqueConstraint(columnNames = {"project_id", "name"})
    }
)
public class ProjectStatus {

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
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // Dự án sở hữu trạng thái này

    // ==========================================
    // STATUS DETAILS (Thông tin trạng thái/cột)
    // ==========================================
    @Column(name = "name", nullable = false, length = 100)
    private String name; // Tên cột (ví dụ: "Cần làm", "Đang tiến hành", "Đã xong")

    @Column(name = "color", length = 7)
    private String color; // Mã màu hex của cột (ví dụ: #FF0000)

    // ==========================================
    // DISPLAY & BEHAVIOR (Hiển thị & Hành vi)
    // ==========================================
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder; // Thứ tự hiển thị của cột trên bảng (ví dụ: 0, 1, 2)

    @Column(name = "is_completed_status", nullable = false)
    @Builder.Default
    private Boolean isCompletedStatus = false; // Cờ xác định đây có phải là trạng thái "Hoàn thành" (Done/Closed) không

}