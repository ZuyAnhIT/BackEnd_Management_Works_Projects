package com.quanlyduan.project_manager_api.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerHealthDashboardResponse {

    private ActiveTenantMetric activeTenants;
    private NewVsChurnMetric newVsChurn;
    private List<TenantByPlan> tenantsByPlan;

    @Data
    @Builder
    public static class ActiveTenantMetric {
        private Long currentActiveCount;
        private Long previousActiveCount;
        private Double growthPercentage;
        private Boolean isPositiveGrowth;
    }

    @Data
    @Builder
    public static class NewVsChurnMetric {
        private Long newTenants;
        private Long churnedTenants;
        private Long netRetention; // (New - Churn): Chỉ số giữ chân ròng
    }

    @Data
    @Builder
    public static class TenantByPlan {
        private String planName;
        private Long tenantCount;
        private Double percentage;
    }
}