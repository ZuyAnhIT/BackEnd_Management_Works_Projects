package com.quanlyduan.project_manager_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO phan hoi chi tiet Giao dich thanh toan (Billing & Transactions).
 * Ho tro hien thi lich su thanh toan, doi soat cong thanh toan (PayOS) 
 * va giai thich logic tinh toan dong tien (Proration).
 */
@Getter
@Setter
@Builder
public class TransactionDetailResponse {

    // ======================================================
    // 1. THONG TIN GIAO DICH (TRANSACTION BASE)
    // ======================================================

    /** Ma giao dich noi bo cua he thong Worknet. */
    private String transactionCode;

    /** Thoi diem khoi tao va thoi diem hoan tat thanh toan thuc te. */
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    /** Trang thai giao dich (vi du: PENDING, SUCCESS, FAILED). */
    private String status;

    /** Phuong thuc thanh toan (vi du: BANK_TRANSFER, CREDIT_CARD). */
    private String paymentMethod;

    // ======================================================
    // 2. CHI TIET DONG TIEN (PRORATION & PRICING)
    // ======================================================

    /** Gia niem yet cua goi cuoc tai thoi diem thanh toan. */
    private BigDecimal originalPrice; 

    /** * So tien duoc khau tru (tu so ngay chua su dung cua goi cuoc cu). 
     * Dung de giai thich cho khach hang tai sao ho chi can tra mot phan tien.
     */
    private BigDecimal deductedAmount; 

    /** So tien thuc te khach hang da thanh toan sau khi tru di phan khau tru. */
    private BigDecimal finalPaidAmount; 

    // ======================================================
    // 3. DOI SOAT CONG THANH TOAN (GATEWAY INFO)
    // ======================================================

    /** Ma tham chieu tu cong thanh toan (vi du: Ma PayOS, Stripe ID). */
    private String gatewayReferenceCode; 

    // ======================================================
    // 4. TINH TRANG GOI CUOC (SUBSCRIPTION CONTEXT)
    // ======================================================

    /** Ten goi cuoc dang su dung (vi du: Pro Plan, Enterprise). */
    private String currentPlanName;

    /** Trang thai thue bao (vi du: ACTIVE, PAST_DUE, EXPIRED). */
    private String subscriptionStatus; 

    /** Thoi han chu ky thanh toan hien tai. */
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;

    /** Co danh dau khach hang da bam huy nhung dang cho het han ky cu hay khong. */
    private boolean isPendingCancel; 

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    /**
     * Constructor mac dinh (No-args) viet tay.
     */
    public TransactionDetailResponse() {
    }

    /**
     * Constructor day du (All-args) viet tay de @Builder hoat dong minh bach.
     */
    public TransactionDetailResponse(String transactionCode, LocalDateTime createdAt, 
                                     LocalDateTime paidAt, String status, String paymentMethod, 
                                     BigDecimal originalPrice, BigDecimal deductedAmount, 
                                     BigDecimal finalPaidAmount, String gatewayReferenceCode, 
                                     String currentPlanName, String subscriptionStatus, 
                                     LocalDateTime currentPeriodStart, LocalDateTime currentPeriodEnd, 
                                     boolean isPendingCancel) {
        this.transactionCode = transactionCode;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.originalPrice = originalPrice;
        this.deductedAmount = deductedAmount;
        this.finalPaidAmount = finalPaidAmount;
        this.gatewayReferenceCode = gatewayReferenceCode;
        this.currentPlanName = currentPlanName;
        this.subscriptionStatus = subscriptionStatus;
        this.currentPeriodStart = currentPeriodStart;
        this.currentPeriodEnd = currentPeriodEnd;
        this.isPendingCancel = isPendingCancel;
    }
}