package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "Role")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private CapDoRole capDo; // SYSTEM, COMPANY, WORKSPACE, PROJECT

    @CreationTimestamp
    private LocalDateTime ngayTao;
}
