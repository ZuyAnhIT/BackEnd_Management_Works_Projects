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
@Table(name = "user_roles", uniqueConstraints = { // Đã dịch
    // Đảm bảo một người dùng không thể có cùng 1 role 2 lần
    @UniqueConstraint(columnNames = {"user_id", "role_id"}) // Đã dịch
})
public class UserRole { // Đã dịch

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Đã dịch

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Đã dịch
    private User user; // Đã dịch

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false) // Đã dịch
    private Role role; // Role (capDo = SYSTEM)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false) // Đã dịch
    private LocalDateTime createdAt; // Đã dịch
}