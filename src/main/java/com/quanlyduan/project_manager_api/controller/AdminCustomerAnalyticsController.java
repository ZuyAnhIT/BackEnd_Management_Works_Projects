package com.quanlyduan.project_manager_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.CustomerHealthDashboardResponse;
import com.quanlyduan.project_manager_api.service.CustomerHealthReportService;

/**
 * Controller handling customer health metrics for system administrators.
 */
@RestController
@RequestMapping("/api/admin/analytics/customers")
public class AdminCustomerAnalyticsController {

    private final CustomerHealthReportService customerHealthReportService;

    public AdminCustomerAnalyticsController(CustomerHealthReportService customerHealthReportService) {
        this.customerHealthReportService = customerHealthReportService;
    }

    /**
     * Retrieves the customer health overview (Active Tenants, New vs Churn, Tenants by Plan).
     * Requires system-level view permissions.
     */
    @GetMapping("/overview")
    @PreAuthorize("@securityService.hasSystemPermission('tenant:view')")
    public ResponseEntity<ApiResponse<CustomerHealthDashboardResponse>> getCustomerHealthOverview(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
            
        CustomerHealthDashboardResponse response = customerHealthReportService.getCustomerHealthOverview(year, month);
        
        return ResponseEntity.ok(ApiResponse.success("Retrieved customer health analytics successfully.", response));
    }
}