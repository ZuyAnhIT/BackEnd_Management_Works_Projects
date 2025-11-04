package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;
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
@Table(name = "KhongGian")
public class KhongGian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idKhongGian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "congTyId", nullable = false)
    private CongTy congTy;

    @Column(nullable = false)
    private String tenKhongGian;

    private String maKhongGian;
    private String moTa;
    private String anhBia;

    @Builder.Default
    private String mauSac = "#3498db";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoiTaoId", nullable = false)
    private NguoiDung nguoiTao;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceStatus trangThai = WorkspaceStatus.HOAT_DONG;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime ngayTao;

    @UpdateTimestamp
    private LocalDateTime ngayCapNhat;
}