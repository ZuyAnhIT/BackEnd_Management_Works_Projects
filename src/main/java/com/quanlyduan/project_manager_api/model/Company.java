package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;
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
@Table(name = "CongTy")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCongTy;

    @Column(nullable = false)
    private String tenCongTy;

    @Column(unique = true)
    private String maCongTy; // Có thể tự động tạo từ tên

    private String moTa;
    private String logo;
    private String diaChi;
    private String soDienThoai;
    private String email;
    private String website;

    @Column(nullable = false)
    private Integer nguoiTaoId; // Chỉ lưu ID người tạo

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyStatus trangThai = CompanyStatus.HOAT_DONG;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime ngayTao;

    @UpdateTimestamp
    private LocalDateTime ngayCapNhat;
}