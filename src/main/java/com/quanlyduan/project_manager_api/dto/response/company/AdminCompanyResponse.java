package com.quanlyduan.project_manager_api.dto.response.company;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO phan hoi thong tin chi tiet Cong ty danh cho Admin.
 * Bao gom thong tin co ban, trang thai xac thuc va chi tiet goi cuoc hien tai.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCompanyResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY INFO)
    // ======================================================
    
    private Integer id;
    
    /** Ten day du cua cong ty/to chuc. */
    private String name;
    
    /** Ma dinh danh duy nhat cua Tenant (vi du: WORKNET_JSC). */
    private String companyCode;
    
    /** Email quan tri va so dien thoai lien he chinh. */
    private String email;
    private String phoneNumber;

    /** Dung luong luu tru hien tai (tinh bang Bytes). */
    private Long currentStorageBytes;

    // ======================================================
    // 2. TRANG THAI VAN HANH (OPERATIONAL STATUS)
    // ======================================================

    /** Co xac nhan doanh nghiep da qua kiem duyet. */
    private Boolean isVerifiedTenant;

    /** * Trang thai hoat dong cua he thong.
     * Gia tri: ACTIVE, SUSPENDED, DELETED.
     */
    private String status; 

    /** Thoi diem cong ty dang ky tham gia he thong. */
    private LocalDateTime createdAt;

    // ======================================================
    // 3. THONG TIN GOI CUOC (SUBSCRIPTION INFO)
    // ======================================================
    // Du lieu duoc tong hop tu company_subscriptions va subscription_plans

    /** Ma code va ten hien thi cua goi cuoc (vi du: PRO_PLAN, Enterprise). */
    private String planCode;
    private String planName;

    /** * Tinh trang thanh toan/su dung cua goi.
     * Gia tri: TRIAL, ACTIVE, EXPIRED, CANCELLED.
     */
    private String subscriptionStatus; 

    /** Ngay het han cua ky thanh toan hien tai. */
    private LocalDateTime currentPeriodEnd;
}