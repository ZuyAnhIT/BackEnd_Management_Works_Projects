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
@Table(name = "company_invitations", uniqueConstraints = { // Đã dịch
    // Đảm bảo không thể mời 1 email 2 lần vào cùng 1 cty nếu lời mời đang PENDING
    @UniqueConstraint(columnNames = {"company_id", "email", "status"}) // Đã dịch
})
public class CompanyInvitation { // Đã dịch

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Đã dịch

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false) // Đã dịch
    private Company company; // Đã dịch

    @Column(nullable = false)
    private String email; // Email người được mời

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false) // Đã dịch
    private Role role; // Role sẽ được gán

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id", nullable = false) // Đã dịch
    private User invitedBy; // Đã dịch // Admin gửi lời mời

    @Column(nullable = false, unique = true)
    private String token; // Token duy nhất (UUID)

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false) // Đã dịch
    private InvitationStatus status = InvitationStatus.PENDING; // Đã dịch

    @Column(name = "expires_at", nullable = false) // Đã dịch
    private LocalDateTime expiresAt; // Đã dịch

    @CreationTimestamp
    @Column(name = "created_at", updatable = false) // Đã dịch
    private LocalDateTime createdAt; // Đã dịch

    @UpdateTimestamp
    @Column(name = "updated_at") // Đã dịch
    private LocalDateTime updatedAt; // Đã dịch
}