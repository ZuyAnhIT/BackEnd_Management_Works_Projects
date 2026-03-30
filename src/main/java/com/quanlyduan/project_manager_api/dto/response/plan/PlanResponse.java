package com.quanlyduan.project_manager_api.dto.response.plan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO phan hoi thong tin chi tiet ve mot Goi cuoc (Subscription Plan).
 * Dung de hien thi danh sach goi cuoc cho nguoi dung so sanh va dang ky.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY INFO)
    // ======================================================
    
    private Integer id;
    
    /** Ma code duy nhat cua goi (vi du: FREE, PRO, ENTERPRISE). */
    private String planCode;
    
    /** Ten hien thi cua goi cuoc. */
    private String name;
    
    /** Mo ta ngan gon ve cac loi ich cua goi. */
    private String description;

    // ======================================================
    // 2. THONG TIN CHI PHI (PRICING)
    // ======================================================

    /** Gia thue bao theo thang. */
    private BigDecimal monthlyPrice;
    
    /** Gia thue bao theo nam (thuong co chiet khau). */
    private BigDecimal yearlyPrice;

    // ======================================================
    // 3. HAN MUC TAI NGUYEN (RESOURCE LIMITS)
    // ======================================================
    // Luu y: Gia tri -1 thuong dai dien cho "Khong gioi han".

    /** So luong thanh vien toi da trong mot Company. */
    private Integer maxUsers;
    
    /** So luong Khong gian lam viec (Workspace) toi da. */
    private Integer maxWorkspaces;
    
    /** So luong Du an (Project) toi da co the tao. */
    private Integer maxProjects;
    
    /** Dung luong luu tru toi da (don vi: GB). */
    private Integer maxStorageGb;

    // ======================================================
    // 4. TINH NANG & CAU HINH (FEATURES & CONFIG)
    // ======================================================

    /** * Danh sach cac tinh nang mo rong duoi dang JSON.
     * Cho phep linh hoat them bot cac tinh nang ma khong can thay doi cau truc DB.
     */
    private JsonNode features;
    
    /** Trang thai cho phep dang ky moi. */
    private Boolean isActive;
    
    /** Thu tu hien thi tren Pricing Page (vi du: 1, 2, 3). */
    private Integer sortOrder;

    // ======================================================
    // 5. THONG TIN HE THONG (SYSTEM METADATA)
    // ======================================================

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}