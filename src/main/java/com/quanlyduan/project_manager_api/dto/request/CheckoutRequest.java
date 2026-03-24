package com.quanlyduan.project_manager_api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequest {
    
    @NotNull(message = "Vui lòng truyền ID Công ty (Company ID).")
    private Integer companyId;
    
    @NotNull(message = "Vui lòng chọn Gói cước (Plan ID).")
    private Integer planId;
    
    @NotNull(message = "Vui lòng chọn Chu kỳ thanh toán (MONTHLY hoặc YEARLY).")
    private String billingCycle;
    
    private String returnUrl; 
    private String cancelUrl;
}