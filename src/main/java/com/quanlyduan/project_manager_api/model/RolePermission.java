// File: src/main/java/com/quanlyduan/project_manager_api/model/RolePermission.java
package com.quanlyduan.project_manager_api.model;

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
// Đặt tên bảng là role_permissions
@Table(name = "role_permissions", 
    uniqueConstraints = {
        // Đảm bảo mỗi Role chỉ có một Permission duy nhất (Unique Role-Permission Pair)
        @UniqueConstraint(columnNames = {"role_id", "permission_id"})
    }
)
/**
 * Entity lưu trữ mối quan hệ Many-to-Many giữa Role và Permission.
 * Đây là bảng trung gian cho việc phân quyền (RBAC).
 */
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role; // Vai trò (Role) liên quan

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission; // Quyền hạn (Permission) liên quan

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo mối quan hệ
}