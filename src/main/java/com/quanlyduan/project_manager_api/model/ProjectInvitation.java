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
 * Entity lưu trữ thông tin về một lời mời tham gia Dự án.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_invitations") 
public class ProjectInvitation { 

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
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // Dự án được mời

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role; // Vai trò dự kiến sẽ được gán

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id", nullable = false)
    private User invitedBy; // Người dùng đã gửi lời mời

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
    private InvitationStatus status = InvitationStatus.PENDING; // Trạng thái lời mời

    // ==========================================
    // TIMESTAMPS (Thời gian hệ thống)
    // ==========================================
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // Thời điểm hết hạn

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng

}