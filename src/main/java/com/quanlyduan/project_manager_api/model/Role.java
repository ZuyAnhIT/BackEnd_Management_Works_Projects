// File: src/main/java/com/quanlyduan/project_manager_api/model/Role.java
package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Set;

@Data
@Entity
@Table(name = "roles")
/**
 * Entity đại diện cho một Vai trò (Role) trong hệ thống.
 * Vai trò được định nghĩa theo cấp độ (Level) và chứa tập hợp các Quyền hạn (Permissions).
 */
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    @Column(name = "role_code", nullable = false, unique = true)
    private String roleCode; // Mã vai trò (Ví dụ: COMPANY_ADMIN)

    @Column(name = "role_name", nullable = false)
    private String roleName; // Tên hiển thị (Ví dụ: Quản trị viên Công ty)

    @Column(name = "description")
    private String description; // Mô tả vai trò

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private RoleLevel level; // Cấp độ vai trò (SYSTEM, COMPANY, WORKSPACE, PROJECT)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo

    // Quan hệ Many-to-Many với Permission
    // Bảng trung gian: role_permissions
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"), // Khóa chính của bảng Role trong bảng trung gian
        inverseJoinColumns = @JoinColumn(name = "permission_id") // Khóa chính của bảng Permission trong bảng trung gian
    )
    private Set<Permission> permissions; // Tập hợp các quyền hạn mà vai trò này có
}