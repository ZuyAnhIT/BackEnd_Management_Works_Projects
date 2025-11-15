package com.quanlyduan.project_manager_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.MyWorkspaceResponse;
import com.quanlyduan.project_manager_api.service.DashboardService;

@RestController
@RequestMapping("/api/user")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/workspaces")
    public ResponseEntity<ApiResponse<List<MyWorkspaceResponse>>> getMyWorkspaces() {
        List<MyWorkspaceResponse> data = dashboardService.getMyWorkspaces();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách workspace thành công.", data));
    }
}
