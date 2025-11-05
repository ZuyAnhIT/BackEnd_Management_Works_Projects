package com.quanlyduan.project_manager_api.model;

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
@Table(name = "NguoiDungRole", uniqueConstraints = {
    // Đảm bảo một người dùng không thể có cùng 1 role 2 lần
    @UniqueConstraint(columnNames = {"nguoiDungId", "roleId"})
})
public class NguoiDungRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idNguoiDungRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoiDungId", nullable = false)
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roleId", nullable = false)
    private Role role; // Role (capDo = SYSTEM)

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime ngayTao;
}