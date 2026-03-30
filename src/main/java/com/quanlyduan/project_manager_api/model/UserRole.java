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
 * Entity trung gian luu tru moi quan he Many-to-Many giua User va Role.
 * Chu yeu dung de gan cac Vai tro cap do HE THONG (SYSTEM) cho nguoi dung.
 */
@Getter
@Setter
@Builder
@Entity
@Table(
    name = "user_roles", 
    uniqueConstraints = { 
        /** Dam bao moi nguoi dung chi co duy nhat mot ban ghi cho mot Vai tro cu the. */
        @UniqueConstraint(columnNames = {"user_id", "role_id"}) 
    }
)
public class UserRole { 

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Nguoi dung duoc gan vai tro. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Vai tro (thuong la cap SYSTEM) duoc gan cho nguoi dung. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // ======================================================
    // 3. THONG TIN HE THONG (AUDIT INFO)
    // ======================================================
    
    /** Thoi diem vai tro duoc gan cho nguoi dung (tu dong sinh boi Hibernate). */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay cho Hibernate.
     */
    public UserRole() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong minh bach.
     */
    public UserRole(Integer id, User user, Role role, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.role = role;
        this.createdAt = createdAt;
    }
}