package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionDetailResponse {
    // 1. Thông tin giao dịch cơ bản
    private String transactionCode;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private String status;
    private String paymentMethod;
    
    // 2. Chi tiết dòng tiền (Giải thích logic Proration)
    private BigDecimal originalPrice; // Giá gốc của gói
    private BigDecimal deductedAmount; // Số tiền được khấu trừ (từ gói cũ)
    private BigDecimal finalPaidAmount; // Số tiền thực tế đã móc hầu bao
    
    // 3. Thông tin đối soát cổng thanh toán
    private String gatewayReferenceCode; // Mã giao dịch của PayOS (ví dụ: FT260...)
    
    // 4. Thông tin Gói cước hiện tại của Công ty (Tình trạng sử dụng)
    private String currentPlanName;
    private String subscriptionStatus; // ACTIVE, PAST_DUE, EXPIRED...
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private boolean isPendingCancel; // Khách có đang chờ hủy gói vào cuối kỳ không?
}