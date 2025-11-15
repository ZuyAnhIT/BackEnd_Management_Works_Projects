package com.quanlyduan.project_manager_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.MyCompanyResponse;
import com.quanlyduan.project_manager_api.service.DashboardService;

@RestController
@RequestMapping("/api/user")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<List<MyCompanyResponse>>> getMyCompanies() {
        List<MyCompanyResponse> companies = dashboardService.getMyCompanies();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách công ty của bạn thành công.", companies));
    }
}
