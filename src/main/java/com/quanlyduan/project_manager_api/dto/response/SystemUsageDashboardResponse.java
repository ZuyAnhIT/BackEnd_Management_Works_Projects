package com.quanlyduan.project_manager_api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemUsageDashboardResponse {

    private Long totalActiveUsers;
    private StorageMetric storageUsage;
    private EngagementMetric engagementStats;

    @Data
    @Builder
    public static class StorageMetric {
        private Long totalUsedBytes;
        private Long totalCapacityBytes;
        private Double usagePercentage;
    }

    @Data
    @Builder
    public static class EngagementMetric {
        private Long newProjectsCreated;
        private Long newTasksCreated;
    }
}