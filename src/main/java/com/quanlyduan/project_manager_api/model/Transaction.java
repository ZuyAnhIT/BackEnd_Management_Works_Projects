package com.quanlyduan.project_manager_api.model;

import com.quanlyduan.project_manager_api.model.common.enums.BillingCycle;
import com.quanlyduan.project_manager_api.model.common.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nối với Công ty
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Nối với Gói cước (Gói mà khách muốn mua)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    // Nối với CompanySubscription (Cập nhật sau khi thanh toán xong)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private CompanySubscription subscription;

    @Column(name = "transaction_code", nullable = false, unique = true, length = 100)
    private String transactionCode; // Mã đơn hàng của mình (VD: WN-2026-0001)

    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId; // Mã đối soát trả về từ PayOS

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 10)
    private String currency; // Mặc định 'VND'

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false)
    private BillingCycle billingCycle; // Enum: MONTHLY, YEARLY

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // VD: 'PAYOS'

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status; // Enum: PENDING, SUCCESS, FAILED, CANCELLED

    // Lưu chuỗi JSON phản hồi từ PayOS để debug. Định dạng kiểu columnDefinition = "JSON" hoặc TEXT.
    @Column(name = "payment_gateway_response", columnDefinition = "TEXT")
    private String paymentGatewayResponse; 

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy; // Ai là người thao tác mua

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}