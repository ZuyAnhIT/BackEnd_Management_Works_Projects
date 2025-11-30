// File: src/main/java/com/quanlyduan/project_manager_api/model/CompanyInvitation.java
package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;
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
// Đặt tên bảng là company_invitations
@Table(name = "company_invitations", 
    uniqueConstraints = { 
        // Đảm bảo không thể mời 1 email 2 lần vào cùng 1 cty nếu lời mời đang PENDING
        @UniqueConstraint(columnNames = {"company_id", "email", "status"}) 
    }
)
/**
 * Entity lưu trữ thông tin về một lời mời tham gia Công ty.
 */
public class CompanyInvitation { 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company; // Công ty gửi lời mời

    @Column(nullable = false)
    private String email; // Email người được mời

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role; // Vai trò sẽ được gán khi chấp nhận lời mời

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id", nullable = false)
    private User invitedBy; // Người dùng (Admin/Manager) đã gửi lời mời

    @Column(nullable = false, unique = true)
    private String token; // Token duy nhất (UUID) để xác thực lời mời

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING; // Trạng thái của lời mời

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // Thời điểm lời mời hết hạn

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo lời mời

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng
}