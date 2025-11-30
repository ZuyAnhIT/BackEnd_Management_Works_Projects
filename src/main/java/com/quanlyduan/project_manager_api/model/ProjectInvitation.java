// File: src/main/java/com/quanlyduan.project_manager_api/model/ProjectInvitation.java
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
@Table(name = "project_invitations") 
/**
 * Entity lưu trữ thông tin về một lời mời tham gia Dự án.
 */
public class ProjectInvitation { 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // ID định danh

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project; // Dự án được mời

    @Column(nullable = false)
    private String email; // Email người được mời

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role; // Vai trò dự kiến sẽ được gán

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id", nullable = false)
    private User invitedBy; // Người dùng đã gửi lời mời

    @Column(nullable = false, unique = true)
    private String token; // Token duy nhất (UUID) để xác thực lời mời

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING; // Trạng thái lời mời

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // Thời điểm hết hạn

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Thời điểm tạo

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng
}