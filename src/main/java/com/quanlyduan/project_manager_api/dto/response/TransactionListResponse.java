package com.quanlyduan.project_manager_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi tom tat danh sach Giao dich (Transaction List).
 * Toi uu cho viec hien thi bang (Table) lich su thanh toan tai giao dien Quan tri.
 */
@Getter
@Setter
@Builder
public class TransactionListResponse {

    // ======================================================
    // 1. THONG TIN DINH DANH (IDENTITY)
    // ======================================================

    /** * Ma giao dich hien thi (vi du: "TXN-2026-001"). 
     * Dung Code thay vi ID de bao mat thong tin kinh doanh cua he thong. 
     */
    private String transactionCode;

    // ======================================================
    // 2. CHI TIET GOI CUOC & TAI CHINH (PLAN & FINANCE)
    // ======================================================

    /** Ten goi cuoc ma khach hang da thanh toan (vi du: "Standard", "Professional"). */
    private String planName;

    /** * Tong so tien thuc thu tu khach hang. 
     * Day la con so cuoi cung sau khi da tinh toan khau tru/thue (neu co). 
     */
    private BigDecimal amount;

    // ======================================================
    // 3. TRANG THAI & THOI GIAN (STATUS & TIMELINE)
    // ======================================================

    /** Trang thai giao dich hien tai (vi du: SUCCESS, PENDING, CANCELLED). */
    private String status;

    /** Thoi diem giao dich duoc khoi tao trong he thong. */
    private LocalDateTime createdAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     * Dam bao Jackson Deserialize JSON mot cach minh bach.
     */
    public TransactionListResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong on dinh.
     */
    public TransactionListResponse(String transactionCode, String planName, BigDecimal amount, 
                                   String status, LocalDateTime createdAt) {
        this.transactionCode = transactionCode;
        this.planName = planName;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }
}