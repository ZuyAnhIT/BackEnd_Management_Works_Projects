package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;
import java.util.Set;

// JPA & Hibernate
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

// Lombok
import lombok.Data;

// Project Enums
import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;

/**
 * Entity đại diện cho một Vai trò (Role) trong hệ thống.
 * Vai trò được định nghĩa theo cấp độ (Level) và chứa tập hợp các Quyền hạn (Permissions).
 */
@Data
@Entity
@Table(name = "roles")
public class Role {

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    // ==========================================
    // ROLE DETAILS (Thông tin vai trò)
    // ==========================================
    @Column(name = "role_code", nullable = false, unique = true)
    private String roleCode; // Mã vai trò (Ví dụ: COMPANY_ADMIN)

    @Column(name = "role_name", nullable = false)
    private String roleName; // Tên hiển thị (Ví dụ: Quản trị viên Công ty)

    @Column(name = "description")
    private String description; // Mô tả vai trò

    // ==========================================
    // ROLE LEVEL (Cấp độ vai trò)
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private RoleLevel level; // Cấp độ vai trò (SYSTEM, COMPANY, WORKSPACE, PROJECT)

    // ==========================================
    // TIMESTAMPS (Thời gian hệ thống)
    // ==========================================
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo

    // ==========================================
    // RELATIONSHIPS (Quan hệ Entity)
    // ==========================================
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