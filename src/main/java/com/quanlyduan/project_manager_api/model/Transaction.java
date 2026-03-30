package com.quanlyduan.project_manager_api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.quanlyduan.project_manager_api.model.common.enums.BillingCycle;
import com.quanlyduan.project_manager_api.model.common.enums.TransactionStatus;

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
 * Entity dai dien cho mot Giao dich thanh toan (Transaction).
 * Luu tru lich su dong tien, thong tin doi soat voi cong thanh toan (PayOS)
 * va cap nhat trang thai thue bao cho Cong ty.
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "transactions")
public class Transaction {

    // ======================================================
    // 1. DINH DANH & MA DOI SOAT (IDENTITY & REFERENCE)
    // ======================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Ma giao dich noi bo (vi du: WN-2026-0001). Dung de hien thi tren hoa don. */
    @Column(name = "transaction_code", nullable = false, unique = true, length = 100)
    private String transactionCode;

    /** Ma giao dich tra ve tu cong thanh toan (PayOS Transaction ID). */
    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;

    // ======================================================
    // 2. LIEN KET THUC THE (RELATIONSHIPS)
    // ======================================================
    
    /** Cong ty thuc hien thanh toan. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /** Goi cuoc (Plan) ma khach hang dang thuc hien mua/nang cap. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    /** * Ban ghi thue bao cua Cong ty. 
     * Se duoc cap nhat ngay sau khi giao dich thanh cong. 
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private CompanySubscription subscription;

    // ======================================================
    // 3. THONG TIN TAI CHINH (FINANCIAL DATA)
    // ======================================================
    
    /** Tong so tien giao dich. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /** Loai tien te (Mac dinh la 'VND'). */
    @Column(length = 10)
    private String currency;

    /** Chu ky thanh toan duoc chon (MONTHLY hoac YEARLY). */
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false)
    private BillingCycle billingCycle;

    // ======================================================
    // 4. TRANG THAI & PHUONG THUC (STATUS & METHOD)
    // ======================================================
    
    /** Phuong thuc thanh toan (vi du: 'PAYOS', 'BANK_TRANSFER'). */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    /** Trang thai giao dich (PENDING, SUCCESS, FAILED, CANCELLED). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    /** * Du lieu phan hoi tho (Raw response) tu PayOS de phuc vu Debug.
     * Luu tru duoi dang chuoi JSON.
     */
    @Column(name = "payment_gateway_response", columnDefinition = "TEXT")
    private String paymentGatewayResponse;

    /** Thoi diem cong thanh toan xac nhan da nhan du tien. */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // ======================================================
    // 5. THONG TIN HE THONG (AUDIT INFO)
    // ======================================================
    
    /** Nguoi dung (Admin/Owner) thuc hien bam nut thanh toan. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ======================================================
    // CONSTRUCTORS (RULE 5 - TRANSPARENCY)
    // ======================================================

    public Transaction() {
    }

    public Transaction(Integer id, String transactionCode, String gatewayTransactionId, 
                       Company company, SubscriptionPlan plan, CompanySubscription subscription, 
                       BigDecimal amount, String currency, BillingCycle billingCycle, 
                       String paymentMethod, TransactionStatus status, 
                       String paymentGatewayResponse, LocalDateTime paidAt, User createdBy, 
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.transactionCode = transactionCode;
        this.gatewayTransactionId = gatewayTransactionId;
        this.company = company;
        this.plan = plan;
        this.subscription = subscription;
        this.amount = amount;
        this.currency = currency;
        this.billingCycle = billingCycle;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paymentGatewayResponse = paymentGatewayResponse;
        this.paidAt = paidAt;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}