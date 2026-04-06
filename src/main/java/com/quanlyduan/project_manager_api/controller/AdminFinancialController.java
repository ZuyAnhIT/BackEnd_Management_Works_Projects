package com.quanlyduan.project_manager_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.FinancialDashboardResponse;
import com.quanlyduan.project_manager_api.service.FinancialReportService;

/**
 * Controller handling financial analytics requests for system administrators.
 */
@RestController
@RequestMapping("/api/admin/analytics")
public class AdminFinancialController {

    private final FinancialReportService financialReportService;

    // Constructor injection for dependencies
    public AdminFinancialController(FinancialReportService financialReportService) {
        this.financialReportService = financialReportService;
    }

    /**
     * Retrieves the financial overview (MRR, revenue by plans, recent transactions).
     * Requires system-level view permissions to access sensitive revenue data.
     * * @param year Optional filter by year.
     * @param month Optional filter by month.
     * @return ApiResponse containing the FinancialDashboardResponse.
     */
    @GetMapping("/financial-overview")
    @PreAuthorize("@securityService.hasSystemPermission('tenant:view')")
    public ResponseEntity<ApiResponse<FinancialDashboardResponse>> getFinancialOverview(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
            
        FinancialDashboardResponse response = financialReportService.getFinancialOverview(year, month);
        
        return ResponseEntity.ok(ApiResponse.success("Retrieved financial overview successfully.", response));
    }
}