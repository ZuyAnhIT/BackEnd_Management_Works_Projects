package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "CongTy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CongTy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCongTy;

    @Column(nullable = false)
    private String tenCongTy;

    @Column(unique = true)
    private String maCongTy;

    private String moTa;
    private String logo;
    private String diaChi;
    private String soDienThoai;
    private String email;
    private String website;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TrangThaiCongTy trangThai = TrangThaiCongTy.HOAT_DONG;

    @ManyToOne
    @JoinColumn(name = "nguoiTaoId", nullable = false)
    private NguoiDung nguoiTao;

    @CreationTimestamp
    private LocalDateTime ngayTao;

    @UpdateTimestamp
    private LocalDateTime ngayCapNhat;
}
