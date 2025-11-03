package com.quanlyduan.project_manager_api.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import com.quanlyduan.project_manager_api.model.common.enums.*;

@Entity
@Table(name = "CongTyThanhVien")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CongTyThanhVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCongTyThanhVien;

    @ManyToOne
    @JoinColumn(name = "congTyId", nullable = false)
    private CongTy congTy;

    @ManyToOne
    @JoinColumn(name = "nguoiDungId", nullable = false)
    private NguoiDung nguoiDung;

    @ManyToOne
    @JoinColumn(name = "roleId", nullable = false)
    private Role role;

    private String chucVu;
    private String phongBan;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TrangThaiThanhVien trangThai = TrangThaiThanhVien.HOAT_DONG;

    @CreationTimestamp
    private LocalDateTime ngayThamGia;

    @UpdateTimestamp
    private LocalDateTime ngayCapNhat;
}

