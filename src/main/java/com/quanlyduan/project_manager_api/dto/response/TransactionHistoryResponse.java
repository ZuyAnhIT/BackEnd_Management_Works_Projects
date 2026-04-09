package com.quanlyduan.project_manager_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionHistoryResponse {
    private Integer id;
    private String transactionCode;       // Mã WN-123
    private String gatewayTransactionId;  // Mã đối soát VNPAY/MOMO
    private String planName;              // Tên gói (Pro, Enterprise)
    private BigDecimal amount;            // Số tiền
    private String currency;              // Tiền tệ (VND)
    private String billingCycle;          // Chu kỳ (MONTHLY/YEARLY)
    private String paymentMethod;         // Phương thức (BANK_TRANSFER, E_WALLET)
    private String status;                // Trạng thái (SUCCESS, FAILED, PENDING)
    private LocalDateTime paidAt;         // Thời gian trả tiền thực tế
    private LocalDateTime createdAt;      // Thời gian tạo lệnh
}