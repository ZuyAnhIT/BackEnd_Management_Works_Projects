package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.TokenStatus;
import com.quanlyduan.project_manager_api.model.common.enums.TokenType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoiDungId", nullable = false)
    private NguoiDung nguoiDung;

    @Column(nullable = false, unique = true)
    private String token; // Sẽ chứa OTP cho việc xác thực email

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType loaiToken;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenStatus trangThai = TokenStatus.HOAT_DONG;

    @Column(nullable = false)
    private LocalDateTime ngayHetHan;

    private String diaChiIp;
    private String userAgent;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime ngayTao;

    private LocalDateTime ngaySuDungCuoi;
}