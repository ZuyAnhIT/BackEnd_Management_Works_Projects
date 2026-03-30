package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.quanlyduan.project_manager_api.model.common.enums.MemberStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Entity luu tru moi quan he thanh vien giua User va Workspace.
 * Day la bang lien ket (Join Table) mo rong, xac dinh vai tro va quyen han 
 * cua nguoi dung ben trong mot Khong gian lam viec cu the.
 */
@Getter
@Setter
@Builder
@Entity
@Table(
    name = "workspace_members", 
    uniqueConstraints = { 
        /** Dam bao moi nguoi dung chi ton tai duy nhat mot ban ghi thanh vien tai mot Workspace. */
        @UniqueConstraint(columnNames = {"workspace_id", "user_id"}) 
    }
)
public class WorkspaceMember { 

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Workspace ma thanh vien nay thuoc ve. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    /** Nguoi dung tro thanh thanh vien. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** * Vai tro (Role) duoc gan cho thanh vien nay tai Workspace.
     * Luu y: Role nay phai co RoleLevel la WORKSPACE.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // ======================================================
    // 3. TRANG THAI & HE THONG (STATUS & AUDIT)
    // ======================================================

    /** * Trang thai hoat dong cua thanh vien trong Workspace.
     * Gia tri: ACTIVE (Hoat dong), SUSPENDED (Tam khoa), REMOVED (Da roi khoi). 
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status;

    /** Thoi diem nguoi dung gia nhap vao Workspace (tu dong sinh boi Hibernate). */
    @CreationTimestamp
    @Column(name = "joined_at", updatable = false, nullable = false)
    private LocalDateTime joinedAt;

    /** Thoi diem cap nhat thong tin thanh vien lan cuoi cung. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    public WorkspaceMember() {
    }

    public WorkspaceMember(Integer id, Workspace workspace, User user, Role role, 
                           MemberStatus status, LocalDateTime joinedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.workspace = workspace;
        this.user = user;
        this.role = role;
        this.status = status;
        this.joinedAt = joinedAt;
        this.updatedAt = updatedAt;
    }
}