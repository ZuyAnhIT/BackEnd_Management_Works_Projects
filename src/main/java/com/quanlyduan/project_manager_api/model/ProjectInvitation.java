package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.quanlyduan.project_manager_api.model.common.enums.InvitationStatus;

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
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity luu tru thong tin loi moi tham gia vao mot Du an cu the (Project Invitation).
 * Dung de quan ly luong xac thuc loi moi qua Email va tu dong gan quyen khi nguoi dung chap nhan.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "project_invitations") 
public class ProjectInvitation { 

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Du an (Project) ma nguoi dung duoc moi tham gia. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** Vai tro (Role) du kien se duoc gan cho nguoi dung ben trong du an nay. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /** Nguoi dung (thuong la Project Manager) da thuc hien gui loi moi. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id", nullable = false)
    private User invitedBy;

    // ======================================================
    // 3. CHI TIET LOI MOI (INVITATION DETAILS)
    // ======================================================
    
    /** Email cua nguoi nhan loi moi. */
    @Column(nullable = false)
    private String email; 

    /** * Ma token duy nhat (UUID) dung de xac thuc trong link kich hoat. 
     * Phai la duy nhat de dam bao tinh bao mat va chong trung lap.
     */
    @Column(nullable = false, unique = true)
    private String token; 

    /** * Trang thai hien tai cua loi moi.
     * Gia tri: PENDING, ACCEPTED, REJECTED, EXPIRED. 
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status; 

    // ======================================================
    // 4. QUAN LY THOI GIAN (TIMELINE & AUDIT)
    // ======================================================
    
    /** Thoi diem loi moi se het han va khong con gia tri xac thuc. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; 

    /** Thoi diem tao ban ghi (tu dong sinh boi Hibernate). */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt; 

    /** Thoi diem cap nhat trang thai loi moi lan cuoi cung. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    public ProjectInvitation() {
    }

    public ProjectInvitation(Integer id, Project project, Role role, User invitedBy, 
                             String email, String token, InvitationStatus status, 
                             LocalDateTime expiresAt, LocalDateTime createdAt, 
                             LocalDateTime updatedAt) {
        this.id = id;
        this.project = project;
        this.role = role;
        this.invitedBy = invitedBy;
        this.email = email;
        this.token = token;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}