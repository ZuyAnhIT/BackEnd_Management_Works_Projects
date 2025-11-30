// File: src/main/java/com/quanlyduan/project_manager_api/model/UserRole.java
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
// Đặt tên bảng là user_roles
@Table(name = "user_roles", 
    uniqueConstraints = { 
        // Đảm bảo một người dùng không thể có cùng 1 role 2 lần (Unique User-Role Pair)
        @UniqueConstraint(columnNames = {"user_id", "role_id"}) 
    }
)
/**
 * Entity lưu trữ mối quan hệ Many-to-Many giữa User và Role.
 * Thường được sử dụng để gán các Role cấp độ SYSTEM cho người dùng.
 */
public class UserRole { 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người dùng sở hữu vai trò

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role; // Vai trò (Role) được gán (thường là Role cấp SYSTEM)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo mối quan hệ
}