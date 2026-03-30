package com.quanlyduan.project_manager_api.dto.response.company;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO phan hoi cai nhin tong the (360-degree view) ve trang thai cua mot Tenant.
 * Giup quan tri vien theo doi thong tin goi cuoc, han muc tai nguyen va cac canh bao he thong.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant360Response {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final int UNLIMITED_LIMIT = -1;

    // ======================================================
    // 1. THONG TIN CONG TY (COMPANY PROFILE)
    // ======================================================
    
    private Integer companyId;
    private String companyName;
    private String email;
    
    /** Trang thai hien tai: ACTIVE, SUSPENDED, v.v. */
    private String status;
    private LocalDateTime createdAt;

    // ======================================================
    // 2. THONG TIN GOI CUOC (SUBSCRIPTION DETAILS)
    // ======================================================
    
    private String planCode;
    private String planName;
    private BigDecimal monthlyPrice;
    
    /** Tinh trang thanh toan: TRIAL, ACTIVE, GRACE_PERIOD, EXPIRED. */
    private String subscriptionStatus;
    
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;

    // ======================================================
    // 3. CHI SO SU DUNG TAI NGUYEN (USAGE VS QUOTA)
    // ======================================================

    /** Han muc nhan su. */
    private long totalMembers;
    private Integer maxUsers; // Su dung UNLIMITED_LIMIT neu khong gioi han

    /** Han muc du an. */
    private long totalProjects;
    private Integer maxProjects;

    /** Han muc bo nho (tinh bang Bytes). */
    private long currentStorageBytes;
    private long maxStorageBytes; 

    // ======================================================
    // 4. CO BAO HIEU CHO FRONTEND (ACTION FLAGS)
    // ======================================================
    
    /** Đanh dau cong ty dang trong thoi gian cho thanh toan (Grace Period). */
    private Boolean isGracePeriod; 
    
    /** Đanh dau da dat gioi han nhan su -> Chan nut "Them nhan vien". */
    private Boolean isUserLimitExceeded; 
    
    /** Đanh dau da dat gioi han du an -> Chan nut "Tao du an". */
    private Boolean isProjectLimitExceeded; 
    
    /** Đanh dau het bo nho -> Chan nut "Tai file len". */
    private Boolean isStorageLimitExceeded;
}