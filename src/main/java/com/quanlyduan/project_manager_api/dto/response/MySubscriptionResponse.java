package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MySubscriptionResponse {
    // 1. Thông tin Gói cước
    private String planName;
    private String planCode;
    private BigDecimal monthlyPrice;
    private BigDecimal yearlyPrice;
    
    // 2. Tình trạng sử dụng
    private String subscriptionStatus; // ACTIVE, PAST_DUE, EXPIRED
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private boolean isCancelAtPeriodEnd; // Có đang chờ hủy không?
    
    // 3. Thông số Quota (Đã dùng / Cho phép)
    private long currentMembers;
    private int maxMembers;
    
    private long currentProjects;
    private int maxProjects;
    
    private long currentStorageBytes;
    private long maxStorageBytes; // -1 nghĩa là Không giới hạn
}