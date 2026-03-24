package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CancelPaymentRequest {
    
    @NotNull(message = "companyId không được để trống")
    private Integer companyId;

    // Frontend có thể truyền lý do (ví dụ: "Đổi ý", "Chọn sai gói"), nếu không có thì để trống
    private String cancellationReason = "Người dùng chủ động hủy thanh toán"; 
}