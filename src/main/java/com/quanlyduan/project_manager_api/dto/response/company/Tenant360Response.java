package com.quanlyduan.project_manager_api.dto.response.company;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class Tenant360Response {
    // 1. Thông tin Công ty
    private Integer companyId;
    private String companyName;
    private String email;
    private String status;
    private LocalDateTime createdAt;

    // 2. Thông tin Gói cước hiện tại
    private String planCode;
    private String planName;
    private BigDecimal monthlyPrice;
    private String subscriptionStatus;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;

    // 3. Chỉ số sử dụng tài nguyên (Usage vs Quota)
    private long totalMembers;
    private Integer maxUsers; // -1 là không giới hạn

    private long totalProjects;
    private Integer maxProjects;

    private long currentStorageBytes;
    private long maxStorageBytes; // Tính từ maxStorageGb * 1024^3
}