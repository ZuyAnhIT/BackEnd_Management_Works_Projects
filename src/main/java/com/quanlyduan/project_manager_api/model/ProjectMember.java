// File: src/main/java/com/quanlyduan.project_manager_api/model/ProjectMember.java
package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_members", 
    uniqueConstraints = {
        // Đảm bảo mỗi người dùng chỉ có một vai trò tại một dự án (Unique Project-User Pair)
        @UniqueConstraint(columnNames = {"project_id", "user_id"})
    }
)
/**
 * Entity lưu trữ mối quan hệ thành viên giữa User và Project.
 */
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // Dự án liên quan

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người dùng liên quan

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role; // Vai trò của thành viên trong dự án

    /**
     * Map với ENUM('ACTIVE', 'REMOVED') hoặc ('ACTIVE', 'SUSPENDED', 'REMOVED').
     * Trạng thái thành viên trong Dự án.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt; // Thời điểm tham gia dự án

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng
}