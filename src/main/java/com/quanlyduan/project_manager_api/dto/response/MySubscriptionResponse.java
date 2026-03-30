package com.quanlyduan.project_manager_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi thong tin chi tiet ve Goi cuoc hien tai cua nguoi dung/cong ty.
 * Cung cap cac chi so ve chi phi, thoi han va han muc tai nguyen (Quota) duoc phep su dung.
 */
@Getter
@Setter
@Builder
public class MySubscriptionResponse {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final long UNLIMITED_VALUE = -1L;

    // ======================================================
    // 1. THONG TIN GOI CUOC (PLAN INFO)
    // ======================================================
    
    /** Ten hien thi va ma dinh danh cua goi cuoc (vi du: "Enterprise", "PRO_PLAN"). */
    private String planName;
    private String planCode;

    /** Muc gia niem yet theo thang va theo nam. */
    private BigDecimal monthlyPrice;
    private BigDecimal yearlyPrice;
    
    // ======================================================
    // 2. TINH TRANG SU DUNG (SUBSCRIPTION STATUS)
    // ======================================================
    
    /** * Trang thai thanh toan hien tai. 
     * Gia tri: ACTIVE (Dang dung), PAST_DUE (Qua han), EXPIRED (Het han).
     */
    private String subscriptionStatus; 

    /** Thoi diem bat dau va ket thuc chu ky thanh toan hien tai. */
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;

    /** Co danh dau nguoi dung da bam huy va dang cho het chu ky de ngat goi. */
    private boolean isCancelAtPeriodEnd; 
    
    // ======================================================
    // 3. THONG SO HAN MUC (RESOURCE QUOTA)
    // ======================================================
    // Luu y: Neu gia tri Max la UNLIMITED_VALUE thi nghia la khong gioi han.

    /** Han muc thanh vien (Da dung / Toi da). */
    private long currentMembers;
    private int maxMembers;
    
    /** Han muc du an (Da dung / Toi da). */
    private long currentProjects;
    private int maxProjects;
    
    /** Han muc dung luong luu tru (Bytes) (Da dung / Toi da). */
    private long currentStorageBytes;
    private long maxStorageBytes;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     */
    public MySubscriptionResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public MySubscriptionResponse(String planName, String planCode, BigDecimal monthlyPrice, 
                                  BigDecimal yearlyPrice, String subscriptionStatus, 
                                  LocalDateTime currentPeriodStart, LocalDateTime currentPeriodEnd, 
                                  boolean isCancelAtPeriodEnd, long currentMembers, int maxMembers, 
                                  long currentProjects, int maxProjects, long currentStorageBytes, 
                                  long maxStorageBytes) {
        this.planName = planName;
        this.planCode = planCode;
        this.monthlyPrice = monthlyPrice;
        this.yearlyPrice = yearlyPrice;
        this.subscriptionStatus = subscriptionStatus;
        this.currentPeriodStart = currentPeriodStart;
        this.currentPeriodEnd = currentPeriodEnd;
        this.isCancelAtPeriodEnd = isCancelAtPeriodEnd;
        this.currentMembers = currentMembers;
        this.maxMembers = maxMembers;
        this.currentProjects = currentProjects;
        this.maxProjects = maxProjects;
        this.currentStorageBytes = currentStorageBytes;
        this.maxStorageBytes = maxStorageBytes;
    }
}