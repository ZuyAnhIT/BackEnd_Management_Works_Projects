package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

// JPA & Hibernate
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Entity đại diện cho một Quyền hạn (Permission) trong hệ thống.
 * Đây là đối tượng cơ sở cho việc phân quyền (Role-Based Access Control).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions") // Đặt tên bảng là permissions
public class Permission {

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    // ==========================================
    // PERMISSION DETAILS (Thông tin quyền hạn)
    // ==========================================
    @Column(name = "permission_code", nullable = false, unique = true)
    private String permissionCode; // Mã quyền hạn (Ví dụ: TASK_CREATE, USER_READ)

    @Column(name = "permission_name", nullable = false)
    private String permissionName; // Tên hiển thị của quyền hạn

    @Column(name = "group_name")
    private String groupName; // Nhóm quyền hạn (Ví dụ: TASK_MANAGEMENT, USER_MANAGEMENT)

    // ==========================================
    // TIMESTAMPS (Thời gian hệ thống)
    // ==========================================
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo

    // ==========================================
    // NOTES (Ghi chú kiến trúc)
    // ==========================================
    // Lưu ý: Không cần map quan hệ ngược lại với RolePermission
    // vì chúng ta ít khi truy vấn trực tiếp từ Permission

}