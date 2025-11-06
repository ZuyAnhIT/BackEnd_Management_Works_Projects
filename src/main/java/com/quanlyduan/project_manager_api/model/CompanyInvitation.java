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
@Table(name = "CongTyLoiMoi", uniqueConstraints = {
    // Đảm bảo không thể mời 1 email 2 lần vào cùng 1 cty nếu lời mời đang PENDING
    @UniqueConstraint(columnNames = {"congTyId", "email", "trangThai"})
})
public class CompanyInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idLoiMoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "congTyId", nullable = false)
    private Company congTy;

    @Column(nullable = false)
    private String email; // Email người được mời

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roleId", nullable = false)
    private Role role; // Role sẽ được gán

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoiMoiId", nullable = false)
    private User nguoiMoi; // Admin gửi lời mời

    @Column(nullable = false, unique = true)
    private String token; // Token duy nhất (UUID)

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus trangThai = InvitationStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime ngayHetHan;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime ngayTao;

    @UpdateTimestamp
    private LocalDateTime ngayCapNhat;
}