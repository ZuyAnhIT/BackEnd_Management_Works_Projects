package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.response.SystemUsageDashboardResponse;

public interface SystemUsageReportService {

    /**
     * Retrieve system usage metrics including total users, storage consumption, and engagement stats.
     *
     * @param targetYear  Target year for engagement stats (null for current year)
     * @param targetMonth Target month for engagement stats (null for current month)
     * @return SystemUsageDashboardResponse containing the aggregated metrics
     */
    SystemUsageDashboardResponse getSystemUsageOverview(Integer targetYear, Integer targetMonth);
}