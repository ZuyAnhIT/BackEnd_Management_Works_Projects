package com.quanlyduan.project_manager_api.dto.response.plan;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO hien thi Goi cuoc danh rieng cho Khach hang (Public/Client).
 * Da loai bo toan bo cac truong nhay cam hoac thong tin noi bo cua he thong.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class PublicPlanResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY INFO)
    // ======================================================
    
    private Integer id;
    
    /** Ma code dinh danh cua goi (vi du: FREE, PRO). */
    private String planCode;
    
    /** Ten hien thi cua goi cuoc tren giao dien. */
    private String name;
    
    /** Mo ta ngan gon ve cac loi ich ma goi cuoc mang lai. */
    private String description;

    // ======================================================
    // 2. THONG TIN CHI PHI (PRICING)
    // ======================================================

    /** Gia thue bao hang thang. */
    private BigDecimal monthlyPrice;
    
    /** Gia thue bao hang nam (thuong dung de hien thi muc tiet kiem). */
    private BigDecimal yearlyPrice;

    // ======================================================
    // 3. HAN MUC TAI NGUYEN (RESOURCE LIMITS)
    // ======================================================
    // Gia tri -1 dai dien cho "Khong gioi han" (Unlimited).

    /** So luong thanh vien toi da. */
    private Integer maxUsers;
    
    /** So luong Khong gian lam viec toi da. */
    private Integer maxWorkspaces;
    
    /** So luong Du an toi da co the khoi tao. */
    private Integer maxProjects;
    
    /** Dung luong luu tru toi da (GB). */
    private Integer maxStorageGb;

    // ======================================================
    // 4. TINH NANG MO RONG (EXTENDED FEATURES)
    // ======================================================

    /** * Danh sach cac tinh nang dac biet duoi dang JSON.
     * Dung de hien thi cac dong "Checklist" tinh nang tren Pricing Page.
     */
    private JsonNode features;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Serialize du lieu sang JSON.
     */
    public PublicPlanResponse() {
    }
}