package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CongTyThanhVien", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"congTyId", "nguoiDungId"})
})
public class CompanyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCongTyThanhVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "congTyId", nullable = false)
    private Company congTy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoiDungId", nullable = false)
    private User nguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roleId", nullable = false)
    private Role role; // Sử dụng quan hệ, không phải ENUM

    private String chucVu;
    private String phongBan;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus trangThai = MemberStatus.HOAT_DONG;

    @CreationTimestamp
    private LocalDateTime ngayThamGia;
    @UpdateTimestamp
    private LocalDateTime ngayCapNhat;
}