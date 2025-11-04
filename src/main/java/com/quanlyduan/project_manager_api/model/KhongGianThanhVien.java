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
@Table(name = "KhongGianThanhVien", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"khongGianId", "nguoiDungId"})
})
public class KhongGianThanhVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idKhongGianThanhVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khongGianId", nullable = false)
    private KhongGian khongGian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoiDungId", nullable = false)
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roleId", nullable = false)
    private Role role; // Sử dụng Role (capDo = WORKSPACE)

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus trangThai = MemberStatus.HOAT_DONG;

    @CreationTimestamp
    private LocalDateTime ngayThamGia;

    @UpdateTimestamp
    private LocalDateTime ngayCapNhat;
}