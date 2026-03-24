package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionListResponse {
    private String transactionCode; // Dùng mã giao dịch thay vì ID để bảo mật
    private String planName;        // Tên gói (Pro, Max...)
    private BigDecimal amount;      // Số tiền ĐÃ THU
    private String status;          // Trạng thái (SUCCESS, PENDING, CANCELLED)
    private LocalDateTime createdAt; // Ngày tạo giao dịch
}