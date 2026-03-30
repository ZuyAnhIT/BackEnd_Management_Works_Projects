package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import com.quanlyduan.project_manager_api.model.common.enums.RoleLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity dai dien cho mot Vai tro (Role) trong he thong.
 * Vai tro duoc dinh nghia theo cap do (RoleLevel) va chua tap hop cac Quyen han (Permissions).
 * Day la thanh phan cot loi cua mo hinh phan quyen RBAC.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "roles")
public class Role {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET QUYEN HAN (PERMISSIONS RELATIONSHIP)
    // ======================================================
    
    /** * Tap hop cac quyen han (Permissions) duoc gan cho vai tro nay.
     * Su dung Set de dam bao khong co quyen bi trung lap.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    // ======================================================
    // 3. THONG TIN VAI TRO (ROLE DETAILS)
    // ======================================================
    
    /** Ma vai tro duy nhat (vi du: "COMPANY_ADMIN", "PROJECT_LEAD"). */
    @Column(name = "role_code", nullable = false, unique = true)
    private String roleCode;

    /** Ten hien thi cho nguoi dung (vi du: "Quan tri vien Cong ty"). */
    @Column(name = "role_name", nullable = false)
    private String roleName;

    /** Mo ta ngan gon ve pham vi va trach nhiem cua vai tro. */
    @Column(name = "description")
    private String description;

    // ======================================================
    // 4. CAP ĐO VAI TRO (ROLE LEVEL)
    // ======================================================
    
    /** * Cap do pham vi cua vai tro.
     * Gia tri: SYSTEM (Toan he thong), COMPANY, WORKSPACE, PROJECT.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private RoleLevel level;

    // ======================================================
    // 5. THONG TIN HE THONG (AUDIT INFO)
    // ======================================================
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay cho Hibernate.
     */
    public Role() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong minh bach.
     */
    public Role(Integer id, Set<Permission> permissions, String roleCode, 
                String roleName, String description, RoleLevel level, 
                LocalDateTime createdAt) {
        this.id = id;
        this.permissions = permissions;
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.description = description;
        this.level = level;
        this.createdAt = createdAt;
    }
}