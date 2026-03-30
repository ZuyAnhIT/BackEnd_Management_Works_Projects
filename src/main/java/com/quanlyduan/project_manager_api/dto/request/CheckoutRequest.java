package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO chua thong tin yeu cau khoi tao giao dich thanh toan (Checkout).
 * Dung de thu thap thong tin goi cuoc va chu ky thanh toan truoc khi chuyen huong sang cong thanh toan.
 */
@Getter
@Setter
public class CheckoutRequest {

    // ======================================================
    // KHAI BAO HANG SO (RULE 6)
    // ======================================================
    public static final String COMPANY_ID_REQUIRED = "Company ID is required";
    public static final String PLAN_ID_REQUIRED = "Plan ID is required";
    public static final String BILLING_CYCLE_REQUIRED = "Billing cycle is required (MONTHLY or YEARLY)";

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTIFICATION)
    // ======================================================

    @NotNull(message = COMPANY_ID_REQUIRED)
    private Integer companyId;

    // ======================================================
    // 2. CHI TIET THANH TOAN (PAYMENT DETAILS)
    // ======================================================

    @NotNull(message = PLAN_ID_REQUIRED)
    private Integer planId;

    @NotNull(message = BILLING_CYCLE_REQUIRED)
    private String billingCycle;

    // ======================================================
    // 3. CAU HINH DIEU HUONG (REDIRECT URLS)
    // ======================================================

    /**
     * URL dieu huong ve khi thanh toan thanh cong.
     */
    private String returnUrl;

    /**
     * URL dieu huong ve khi nguoi dung huy giao dich.
     */
    private String cancelUrl;

    // ======================================================
    // CONSTRUCTOR (RULE 5)
    // ======================================================

    /**
     * Constructor mac dinh phuc vu cho viec Deserialize JSON tu Client.
     */
    public CheckoutRequest() {
    }
}