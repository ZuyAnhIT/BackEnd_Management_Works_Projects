// File: src/main/java/com/quanlyduan/project_manager_api/model/WorkspaceMember.java
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
// Đặt tên bảng là workspace_members
@Table(name = "workspace_members", 
    uniqueConstraints = { 
        // Đảm bảo mỗi người dùng chỉ có một vai trò tại một Workspace (Unique Workspace-User Pair)
        @UniqueConstraint(columnNames = {"workspace_id", "user_id"}) 
    }
)
/**
 * Entity lưu trữ mối quan hệ thành viên giữa User và Workspace.
 * Đây là bảng liên kết (Join Table) mở rộng.
 */
public class WorkspaceMember { 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh của mối quan hệ

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace; // Workspace liên quan

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người dùng liên quan

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role; // Vai trò của thành viên trong Workspace (Role Level = WORKSPACE)

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE; // Trạng thái thành viên (ACTIVE, SUSPENDED, REMOVED)

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt; // Thời điểm tham gia/tạo bản ghi

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng
}