package com.quanlyduan.project_manager_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinancialDashboardResponse {

    private MrrMetric mrr;
    private List<RevenueByPlan> revenueByPlans;
    private List<TransactionRecord> recentTransactions;

    @Data
    @Builder
    public static class MrrMetric {
        private BigDecimal currentMonthRevenue;
        private BigDecimal previousMonthRevenue;
        private Double growthPercentage; 
        private Boolean isPositiveGrowth;
    }

    @Data
    @Builder
    public static class RevenueByPlan {
        private String planName;
        private BigDecimal totalRevenue;
        private Double percentage; 
    }

    @Data
    @Builder
    public static class TransactionRecord {
        private String transactionCode;
        private String companyName;
        private String planName;
        private BigDecimal amount;
        private LocalDateTime paidAt;
        private String status;
    }
}