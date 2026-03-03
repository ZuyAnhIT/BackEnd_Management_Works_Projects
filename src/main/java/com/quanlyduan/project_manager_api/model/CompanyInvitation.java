package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// Lombok
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Project Enums
import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;

/**
 * Entity lưu trữ thông tin về một lời mời tham gia Công ty.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "company_invitations", 
    uniqueConstraints = { 
        // Đảm bảo không thể mời 1 email 2 lần vào cùng 1 cty nếu lời mời đang PENDING
        @UniqueConstraint(columnNames = {"company_id", "email", "status"}) 
    }
)
public class CompanyInvitation { 

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
    @JoinColumn(name = "company_id", nullable = false)
    private Company company; // Công ty gửi lời mời

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role; // Vai trò sẽ được gán khi chấp nhận lời mời

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id", nullable = false)
    private User invitedBy; // Người dùng (Admin/Manager) đã gửi lời mời

    // ==========================================
    // INVITATION DETAILS (Thông tin lời mời)
    // ==========================================
    @Column(nullable = false)
    private String email; // Email người được mời

    @Column(nullable = false, unique = true)
    private String token; // Token duy nhất (UUID) để xác thực lời mời

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING; // Trạng thái của lời mời

    // ==========================================
    // TIMESTAMPS (Thời gian)
    // ==========================================
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // Thời điểm lời mời hết hạn

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo lời mời

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng

}