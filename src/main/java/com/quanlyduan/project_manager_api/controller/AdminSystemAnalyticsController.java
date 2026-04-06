package com.quanlyduan.project_manager_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.SystemUsageDashboardResponse;
import com.quanlyduan.project_manager_api.service.SystemUsageReportService;

/**
 * Controller handling system usage and resource analytics for system administrators.
 */
@RestController
@RequestMapping("/api/admin/analytics/system")
public class AdminSystemAnalyticsController {

    private final SystemUsageReportService systemUsageReportService;

    public AdminSystemAnalyticsController(SystemUsageReportService systemUsageReportService) {
        this.systemUsageReportService = systemUsageReportService;
    }

    /**
     * Retrieves system usage metrics including user count, storage consumption, and engagement levels.
     * Requires system-level view permissions.
     */
    @GetMapping("/overview")
    @PreAuthorize("@securityService.hasSystemPermission('tenant:view')")
    public ResponseEntity<ApiResponse<SystemUsageDashboardResponse>> getSystemUsageOverview(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
            
        SystemUsageDashboardResponse response = systemUsageReportService.getSystemUsageOverview(year, month);
        
        return ResponseEntity.ok(ApiResponse.success("Retrieved system usage analytics successfully.", response));
    }
}