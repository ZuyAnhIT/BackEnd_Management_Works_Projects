package com.quanlyduan.project_manager_api.dto.response.company;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminCompanyResponse {
    // Thông tin cơ bản của Company
    private Integer id;
    private String name;
    private String companyCode;
    private String email;
    private String phoneNumber;
    private Long currentStorageBytes;
    private Boolean isVerifiedTenant;
    private String status; // ACTIVE, SUSPENDED, DELETED
    private LocalDateTime createdAt;
    
    // Thông tin Gói cước (Lấy từ bảng company_subscriptions và subscription_plans)
    private String planCode;
    private String planName;
    private String subscriptionStatus; // TRIAL, ACTIVE, EXPIRED...
    private LocalDateTime currentPeriodEnd; // Hạn dùng của gói
}