package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity trung gian luu tru moi quan he Many-to-Many giua Role va Permission.
 * Thiet lap ma tran quyen han cho he thong, xac dinh moi Vai tro se co nhung Quyen han nao.
 */
@Getter
@Setter
@Builder
@Entity
@Table(
    name = "role_permissions", 
    uniqueConstraints = {
        /** Dam bao moi cap Role-Permission la duy nhat, tranh trung lap quyen han trong cung mot vai tro. */
        @UniqueConstraint(columnNames = {"role_id", "permission_id"})
    }
)
public class RolePermission {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Vai tro (Role) duoc gan quyen. Su dung LAZY fetch de toi uu tai nguyen. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /** Quyen han (Permission) cu the duoc gan cho Vai tro. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    // ======================================================
    // 3. THONG TIN HE THONG (AUDIT INFO)
    // ======================================================
    
    /** Thoi diem quyen han nay duoc thiet lap cho Vai tro (tu dong sinh boi Hibernate). */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay cho Hibernate.
     */
    public RolePermission() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong minh bach.
     */
    public RolePermission(Integer id, Role role, Permission permission, LocalDateTime createdAt) {
        this.id = id;
        this.role = role;
        this.permission = permission;
        this.createdAt = createdAt;
    }
}