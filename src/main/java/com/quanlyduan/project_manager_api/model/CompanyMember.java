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
 * Entity luu tru moi quan he thanh vien giua User va Company.
 * Day la bang lien ket (Join Table) mo rong, chua thong tin chi tiet ve vai tro 
 * va vi tri cong tac cua nguoi dung trong mot to chuc.
 */
@Getter
@Setter
@Builder
@Entity
@Table(
    name = "company_members", 
    uniqueConstraints = { 
        /** Dam bao moi nguoi dung chi ton tai duy nhat mot ban ghi thanh vien tai mot Cong ty. */
        @UniqueConstraint(columnNames = {"company_id", "user_id"}) 
    }
)
public class CompanyMember { 

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Cong ty ma thanh vien nay thuoc ve. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /** Nguoi dung tro thanh thanh vien. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Vai tro (Role) duoc gan cho thanh vien nay ben trong Cong ty. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // ======================================================
    // 3. CHI TIET CONG TAC (MEMBER DETAILS)
    // ======================================================
    
    /** Chuc danh hoac vi tri cong viec (vi du: "Senior Dev", "PM"). */
    @Column(name = "job_title")
    private String jobTitle;

    /** Phong ban truc thuoc (vi du: "Tech", "HR"). */
    @Column(name = "department")
    private String department;

    /** * Trang thai hoat dong cua thanh vien trong Cong ty.
     * Gia tri: ACTIVE (Dang lam viec), SUSPENDED (Tam dinh chi), REMOVED (Da nghi viec). 
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status;

    // ======================================================
    // 4. QUAN LY THOI GIAN (TIMELINE)
    // ======================================================
    
    /** Thoi diem nguoi dung gia nhap vao Cong ty (tu dong sinh bơi Hibernate). */
    @CreationTimestamp
    @Column(name = "joined_at", updatable = false, nullable = false)
    private LocalDateTime joinedAt;

    /** Thoi diem cap nhat thong tin thanh vien lan cuoi. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    public CompanyMember() {
    }

    public CompanyMember(Integer id, Company company, User user, Role role, 
                         String jobTitle, String department, MemberStatus status, 
                         LocalDateTime joinedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.company = company;
        this.user = user;
        this.role = role;
        this.jobTitle = jobTitle;
        this.department = department;
        this.status = status;
        this.joinedAt = joinedAt;
        this.updatedAt = updatedAt;
    }
}