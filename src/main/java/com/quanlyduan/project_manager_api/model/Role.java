package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRole;

    @Column(nullable = false, unique = true)
    private String maRole;

    @Column(nullable = false)
    private String tenRole;

    private String moTa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleLevel capDo; // Enum: SYSTEM, COMPANY, WORKSPACE, PROJECT

}