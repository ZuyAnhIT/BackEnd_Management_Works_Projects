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

    
    // 4. CỜ BÁO HIỆU CHO FRONTEND (FLAGS)
    // Đánh dấu công ty đang trong những ngày ân hạn (Grace Period) chờ cắt gói
    private Boolean isGracePeriod; 
    
    // Đánh dấu đã đạt (hoặc vượt) giới hạn nhân sự -> Frontend làm mờ nút "Thêm nhân viên"
    private Boolean isUserLimitExceeded; 
    
    // Đánh dấu đã đạt (hoặc vượt) giới hạn dự án -> Frontend làm mờ nút "Tạo dự án"
    private Boolean isProjectLimitExceeded; 
    
    // Đánh dấu đã đầy bộ nhớ -> Frontend làm mờ nút "Tải file lên"
    private Boolean isStorageLimitExceeded;
}