package com.quanlyduan.project_manager_api.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.quanlyduan.project_manager_api.model.common.enums.CompanyStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity dai dien cho mot Cong ty (Tenant) - Cap do cao nhat trong he thong SaaS Worknet.
 * Quan ly thong tin hanh chinh, tai nguyen luu tru va trang thai dang ky dich vu.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "companies")
public class Company {

    // ======================================================
    // 1. DINH DANH DU LIEU (PRIMARY KEY)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ======================================================
    // 2. THONG TIN CO BAN (BASIC INFO)
    // ======================================================
    
    /** Ten giao dich cua Cong ty. */
    @Column(nullable = false)
    private String name;

    /** Ma dinh danh duy nhat cua Cong ty (vi du: WN-APP-2026). */
    @Column(name = "company_code", unique = true)
    private String companyCode;

    /** Mo ta ngan gon ve linh vuc hoat dong hoac quy mo. */
    @Column(name = "description")
    private String description;

    /** Duong dan URL den anh dai dien/logo chinh thuc. */
    @Column(name = "logo_url")
    private String logoUrl;

    // ======================================================
    // 3. THONG TIN LIEN HE (CONTACT INFO)
    // ======================================================
    
    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "website")
    private String website;

    // ======================================================
    // 4. QUAN LY TAI NGUYEN SAAS (RESOURCE TRACKING)
    // ======================================================
    
    /** * Tong dung luong tep tin hien tai dang su dung (Bytes). 
     * Dung de doi chieu voi han muc cua goi cuoc (Subscription Plan). 
     */
    @Column(name = "current_storage_bytes")
    private Long currentStorageBytes;

    /** Co danh dau Cong ty da duoc xac thuc danh tinh (Verified Tenant). */
    @Column(name = "is_verified_tenant")
    private Boolean isVerifiedTenant;

    // ======================================================
    // 5. TRANG THAI & HE THONG (STATE & AUDIT)
    // ======================================================
    
    /** ID cua nguoi dung khoi tao Cong ty (Chu so huu/Creator). */
    @Column(name = "created_by_id", nullable = false)
    private Integer createdById;

    /** * Trang thai hoat dong cua Cong ty tren he thong.
     * Gia tri: ACTIVE, SUSPENDED (Tam khoa), DELETED. 
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CompanyStatus status;

    // ======================================================
    // 6. LIEN KET & THOI GIAN (RELATIONS & TIMESTAMPS)
    // ======================================================
    
    /** Danh sach cac lich su dang ky va gia han goi cuoc. */
    @Builder.Default
    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<CompanySubscription> subscriptions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    public Company() {
    }

    public Company(Integer id, String name, String companyCode, String description, 
                   String logoUrl, String address, String phoneNumber, String email, 
                   String website, Long currentStorageBytes, Boolean isVerifiedTenant, 
                   Integer createdById, CompanyStatus status, 
                   List<CompanySubscription> subscriptions, LocalDateTime createdAt, 
                   LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.companyCode = companyCode;
        this.description = description;
        this.logoUrl = logoUrl;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.website = website;
        this.currentStorageBytes = currentStorageBytes;
        this.isVerifiedTenant = isVerifiedTenant;
        this.createdById = createdById;
        this.status = status;
        this.subscriptions = subscriptions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}