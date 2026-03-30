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
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity luu tru thong tin loi moi gia nhap Cong ty (Company Invitation).
 * Quan ly luong xac thuc qua Email, thoi han loi moi va vai tro du kien se gan cho thanh vien moi.
 */
@Getter
@Setter
@Builder
@Entity
@Table(
    name = "company_invitations", 
    uniqueConstraints = { 
        /** Ngon chan viec gui trung loi mời khi trang thai van dang PENDING. */
        @UniqueConstraint(columnNames = {"company_id", "email", "status"}) 
    }
)
public class CompanyInvitation { 

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Cong ty phat hanh loi moi. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /** Vai tro se duoc tu dong gan cho nguoi dung sau khi chap nhan loi moi thanh cong. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /** Nguoi dung (Admin/Manager) truc tiep thuc hien gui loi moi nay. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id", nullable = false)
    private User invitedBy;

    // ======================================================
    // 3. CHI TIET LOI MOI (INVITATION DETAILS)
    // ======================================================
    
    /** Email cua nguoi nhan loi moi. */
    @Column(nullable = false)
    private String email; 

    /** * Ma token duy nhat (UUID) dung de xac thuc trong link gui qua Email. 
     * Bat buoc phai duy nhat de tranh xung dot xac thuc.
     */
    @Column(nullable = false, unique = true)
    private String token; 

    /** * Trang thai hien tai cua loi moi.
     * Gia tri: PENDING (Cho), ACCEPTED (Da nhan), REJECTED (Tu choi), EXPIRED (Het han). 
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status; 

    // ======================================================
    // 4. QUAN LY THOI GIAN (TIMELINE)
    // ======================================================
    
    /** Thoi diem loi moi khong con hieu luc de xac thuc. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; 

    /** Thoi diem khoi tao ban ghi (tu dong sinh bơi Hibernate). */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt; 

    /** Lan cuoi cung ban ghi loi moi co su thay doi trang thai. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    public CompanyInvitation() {
    }

    public CompanyInvitation(Integer id, Company company, Role role, User invitedBy, 
                             String email, String token, InvitationStatus status, 
                             LocalDateTime expiresAt, LocalDateTime createdAt, 
                             LocalDateTime updatedAt) {
        this.id = id;
        this.company = company;
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