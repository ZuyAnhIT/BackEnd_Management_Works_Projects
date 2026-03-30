package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.quanlyduan.project_manager_api.model.common.enums.WorkspaceStatus;

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
 * Entity dai dien cho mot Workspace (Khong gian lam viec).
 * Workspace dong vai tro la container trung gian, gom nhom cac Du an 
 * thuoc cung mot phong ban hoac linh vuc trong pham vi mot Cong ty.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "workspaces")
public class Workspace {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Cong ty so huu Workspace nay. Su dung LAZY fetch de toi uu hieu suat. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /** Nguoi dung thuc hien khoi tao Workspace. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false, updatable = false)
    private User createdBy;

    // ======================================================
    // 3. THONG TIN CO BAN (BASIC INFORMATION)
    // ======================================================
    
    /** Ten hien thi cua Workspace (vi du: "Phong Cong nghe", "Team Marketing"). */
    @Column(name = "name", nullable = false)
    private String name;

    /** Ma code viet tat de dinh danh nhanh (vi du: "DEV", "HR"). */
    @Column(name = "workspace_code")
    private String workspaceCode;

    /** Mo ta chi tiet ve muc dich su dung cua khong gian lam viec nay. */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ======================================================
    // 4. GIAO DIEN & TRANG THAI (UI & STATUS)
    // ======================================================
    
    /** Anh bia cua Workspace de hien thi tren Dashboard. */
    @Column(name = "cover_image_url")
    private String coverImageUrl;

    /** Ma mau HEX chu dao dung de phan biet nhanh tren giao dien Sidebar. */
    @Column(name = "color", length = 7)
    private String color;

    /** * Trang thai van hanh hien tai.
     * Gia tri: ACTIVE (Hoat dong), ARCHIVED (Luu tru), DELETED (Xoa mem). 
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkspaceStatus status;

    // ======================================================
    // 5. THONG TIN HE THONG (AUDIT INFO)
    // ======================================================
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    public Workspace() {
    }

    public Workspace(Integer id, Company company, User createdBy, String name, 
                     String workspaceCode, String description, String coverImageUrl, 
                     String color, WorkspaceStatus status, LocalDateTime createdAt, 
                     LocalDateTime updatedAt) {
        this.id = id;
        this.company = company;
        this.createdBy = createdBy;
        this.name = name;
        this.workspaceCode = workspaceCode;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.color = color;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}